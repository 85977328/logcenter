/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.mapreduce;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.panguso.lc.analysis.format.service.IAnalysisService;

/**
 * 
 * 
 * @author piaohailin
 * @date 2013-4-9
 */
public class FormatAnalysisMapper extends Mapper<Object, Text, Text, Text> {
	private ApplicationContext applicationContext;
	private IAnalysisService analysisService;
	private static final Text blank = new Text();

	// private Text line = new Text();
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
		analysisService = applicationContext.getBean(IAnalysisService.class);
		DistributedCache.getLocalCacheFiles(context.getConfiguration());
	}

	@Override
	protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		List<String> result = null;
		try {
			result = analysisService.getFormatedString(value.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (CollectionUtils.isNotEmpty(result)) {
			for (String formatedLog : result) {
				// System.out.println(formatedLog);
				context.write(new Text(formatedLog), blank);
			}
		}
	}

	@Override
	protected void cleanup(Context context) throws IOException, InterruptedException {
	}

}
