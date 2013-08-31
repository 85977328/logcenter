/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format;

import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.panguso.lc.analysis.format.mapreduce.FormatAnalysisMapper;
import com.panguso.lc.analysis.format.mapreduce.FormatAnalysisReducer;
import com.panguso.lc.analysis.format.mapreduce.TextOutputFormat;
import com.panguso.lc.analysis.format.service.IAnalysisService;

/**
 * 
 * map/reduce的入口类
 * 
 * @author piaohailin
 * @date 2013-4-9
 */
public class Logcenter extends Configured implements Tool {
	private static Logger logger = LoggerFactory.getLogger(Logcenter.class);
	private static ApplicationContext context;
	private static IAnalysisService analysisService;
	String libPath;
	String confPath;
	String srcPath;
	String destPath;
	String archivePath;

	@Override
	public int run(String[] args) throws Exception {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Properties prop = context.getBean("configProperties", Properties.class);
		// 初始化参数
		// String time = new DateTime().toString("yyyyMMddHH");

		// hadoop.lib=/application/format/lib/
		// hadoop.conf=/application/format/conf/
		// hadoop.src=/log/src/
		// hadoop.dest=/log/dest/
		// hadoop.archive=/log/archive/
		libPath = prop.getProperty("hadoop.lib");
		confPath = prop.getProperty("hadoop.conf");
		srcPath = prop.getProperty("hadoop.src");
		destPath = prop.getProperty("hadoop.dest");
		archivePath = prop.getProperty("hadoop.archive");
		Configuration conf = getConf();
		logger.info("libPath=" + libPath);
		logger.info("confPath=" + confPath);
		logger.info("srcPath=" + srcPath);
		logger.info("destPath=" + destPath);
		logger.info("archivePath=" + archivePath);

		FileSystem fs = FileSystem.get(conf);
		// 得到文件系统的实例--jar包
		FileStatus[] fJars = fs.listStatus(new Path(libPath));
		for (FileStatus fileStatus : fJars) {
			String jar = libPath + fileStatus.getPath().getName();
			DistributedCache.addFileToClassPath(new Path(jar), conf, FileSystem.get(conf));
		}
		// 得到文件系统的实例--配置文件
		FileStatus[] fProp = fs.listStatus(new Path(confPath));
		for (FileStatus fileStatus : fProp) {
			DistributedCache.addArchiveToClassPath(new Path(confPath + fileStatus.getPath().getName()), conf, FileSystem.get(conf));
		}
		FileStatus[] fDirs = fs.listStatus(new Path(srcPath));
		if (fDirs != null && fDirs.length > 0) {
			for (FileStatus file : fDirs) {
				// 确定dir
				String currentTime = file.getPath().getName();
				String srcPathWithTime = srcPath + currentTime + "/";
				String destPathWithTime = destPath + currentTime + "/";
				String archPathWithTime = archivePath + currentTime + "/";
				// 如果解析成功，则忽略此时间
				if (analysisService.isSuccessful(currentTime)) {
					continue;
				}

				// 如果解析失败，或者解析job没有执行过，则解析

				// 如果输出目录存在，则删除
				fs.delete(new Path(destPathWithTime), true);

				// 如果输入目录不存在，则退出
				// if (!fs.exists(new Path(srcPathWithTime))) {
				// logger.warn("outPath does not exist,inputPath=" +
				// srcPathWithTime);
				// analysisService.saveFailureJob(job.getJobName(),
				// currentTime);
				// return -1;
				// }
				// 替换classpath中的";"为":"
				Job job = new Job(conf);
				String jars = job.getConfiguration().get("mapred.job.classpath.files");
				job.getConfiguration().set("mapred.job.classpath.files", jars.replace(";", ":"));
				logger.info("current dir=" + currentTime);
				job.setJobName("format_" + currentTime);

				job.setJarByClass(Logcenter.class);
				job.setMapperClass(FormatAnalysisMapper.class);
				job.setReducerClass(FormatAnalysisReducer.class);
				job.setCombinerClass(FormatAnalysisReducer.class);
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				// job.setNumReduceTasks(0);
				// //不需要reduce，提高性能，但是解析后的文件会很多，对namenode压力比较大
				FileInputFormat.addInputPath(job, new Path(srcPathWithTime));
				FileOutputFormat.setOutputPath(job, new Path(destPathWithTime));

				// 取得执行结果
				boolean result = false;
				try {
					result = job.waitForCompletion(true);
				} catch (FileAlreadyExistsException e) {
					logger.warn(e.getMessage(), e);
				}
				if (!result) {
					logger.warn("job execute failure!");
					analysisService.saveFailureJob(job.getJobName(), currentTime);
					continue;
					// return -1;
				}

				// 归档,如果目标归档文件夹存在，则删除
				fs.delete(new Path(archPathWithTime), true);
				fs.rename(new Path(srcPathWithTime), new Path(archPathWithTime));
				analysisService.saveSuccessJob(job.getJobName(), currentTime);
			}
		}

		FileSystem.closeAll();
		return 0;
	}
}
