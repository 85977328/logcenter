/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.rubyeye.xmemcached.XMemcachedClientBuilder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.panguso.lc.analysis.format.dao.ILogFormatDao;
import com.panguso.lc.analysis.format.dao.ILogInfoDao;
import com.panguso.lc.analysis.format.dao.ILogJobDao;
import com.panguso.lc.analysis.format.dao.ILogRecordDao;
import com.panguso.lc.analysis.format.dao.ILogSystemDao;
import com.panguso.lc.analysis.format.entity.LogFormat;
import com.panguso.lc.analysis.format.entity.LogInfo;
import com.panguso.lc.analysis.format.entity.LogJob;
import com.panguso.lc.analysis.format.entity.LogRecord;
import com.panguso.lc.analysis.format.entity.LogSystem;
import com.panguso.lc.analysis.format.service.IAnalysisService;

/**
 * @author piaohailin
 * @date 2012-8-30
 */
@Service
public class AnalysisServiceImpl implements IAnalysisService {

	private Logger logger = LoggerFactory.getLogger(AnalysisServiceImpl.class);

	@Autowired
	private ILogRecordDao logRecordDao;

	@Autowired
	private ILogFormatDao logFormatDao;

	@Autowired
	private ILogInfoDao logInfoDao;

	@Autowired
	private ILogSystemDao logSystemDao;

	@Autowired
	private ILogJobDao logJobDao;

	@Autowired
	private XMemcachedClientBuilder client;

//    private String        logSeparate = ",";
	private String logSeparate = "\1";

	@Override
	public List<LogFormat> getFormat(String logKey) {
//		try {
//			List<LogFormat> tmp = cacheRemoteService.get(logKey);
//			if (tmp != null) {
//				return tmp;
//			}
//		} catch (Exception e) {
//			logger.warn(e.getMessage(), e);
//		}

		Map<String, Object> param1 = new HashMap<String, Object>();
		param1.put("logKey", logKey);
		List<LogInfo> logs = logInfoDao.find(param1);
		if (CollectionUtils.isEmpty(logs)) {
			return null;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("logId", logs.get(0).getId());
		// 取得格式
		List<LogFormat> formats = logFormatDao.find(params);
		// 排序
		Collections.sort(formats);

//		// 设置缓存,半小时
//		try {
//			cacheRemoteService.set(logKey, formats, 30 * 60 * 1000);
//		} catch (Exception e) {
//			logger.warn(e.getMessage(), e);
//		}

		// 设置值
		return formats;
	}

	@Override
	public boolean checkRecord(String systemKey, String logKey) {
		Map<String, Object> param1 = new HashMap<String, Object>();
		param1.put("logKey", logKey);
		List<LogInfo> logs = logInfoDao.find(param1);
		if (CollectionUtils.isEmpty(logs)) {
			return false;
		}

		Map<String, Object> param2 = new HashMap<String, Object>();
		param2.put("systemKey", systemKey);
		List<LogSystem> systems = logSystemDao.find(param2);
		if (CollectionUtils.isEmpty(systems)) {
			return false;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("logId", logs.get(0).getId());
		params.put("systemId", systems.get(0).getId());
		List<LogRecord> result = logRecordDao.find(params);
		if (CollectionUtils.isNotEmpty(result)) {
			return true;
		}
		return false;
	}

	@Override
	public void setValue(Map<String, String> params, List<LogFormat> formats) {
		for (Iterator<LogFormat> iterator = formats.iterator(); iterator.hasNext();) {
			LogFormat logFormat = iterator.next();
			String key = logFormat.getLogParameter().getParameterName();
			String value = params.get(key);
			if (StringUtils.isEmpty(value)) {
				value = "";
			}
			logFormat.setValue(value);
		}
	}

	@Override
	public List<String> getFormatedString(String line) throws Exception {
		// 解析字符串

		Map<String, String> params = this.getMap(line);
		// 取得utmst
		String submitType = StringUtils.isEmpty(params.get("utmst")) ? "single" : params
		        .get("utmst"); // 提交类型，值固定,single;array

		if (!"single".equals(submitType) && !"array".equals(submitType)) {
			return null;
		}

		// 临时注释掉，此处是有效代码
//		String version = params.get("utmwv"); // 组件版本
//		String random = params.get("utmn"); // 请求随机码
		String systemKey = params.get("utmac"); // 系统唯一标识

		List<String> result = new ArrayList<String>();

		/* 单条日志 */
		if ("single".equals(submitType)) {
			String logKey = params.get("utmlid");
//			System.out.println("utmlid=" + logKey + ",utmac=" + systemKey);
			// 验证日志是否注册
			if (StringUtils.isEmpty(logKey)) {
				return result;
			}
			if (!this.checkRecord(systemKey, logKey)) {
				return result;
			}
			// 取得日志格式
			List<LogFormat> formats = this.getFormat(logKey);
			// 设置日志里的值
			for (Iterator<LogFormat> iterator = formats.iterator(); iterator.hasNext();) {
				LogFormat logFormat = iterator.next();
				String key = logFormat.getLogParameter().getParameterName();
				String value = params.get(key);
				if (StringUtils.isEmpty(value)) {
					value = "";
				}
				logFormat.setValue(value);
			}
			// 按照格式输出
			StringBuilder singleLog = new StringBuilder();
			singleLog.append(systemKey);
			singleLog.append(logSeparate);
			singleLog.append(logKey);
			singleLog.append(logSeparate);
			for (Iterator<LogFormat> iterator = formats.iterator(); iterator.hasNext();) {
				LogFormat logFormat = iterator.next();
				singleLog.append(logFormat.getValue());
				if (iterator.hasNext()) {
					singleLog.append(logSeparate);
				}
			}
			result.add(singleLog.toString());
			return result;
		}

		/* 多条日志 */
		if ("array".equals(submitType)) {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				String arrayIndex = "logs[" + i + "].";
				String logKey = params.get(arrayIndex + "utmlid"); // logId
				// 验证日志是否注册
				if (StringUtils.isEmpty(logKey)) {
					break;
				}
				if (!this.checkRecord(systemKey, logKey)) {
					break;
				}
				// 取得日志格式
				List<LogFormat> formats = this.getFormat(logKey);

				// 设置日志里的值
				for (Iterator<LogFormat> iterator = formats.iterator(); iterator.hasNext();) {
					LogFormat logFormat = iterator.next();
					String key = arrayIndex + logFormat.getLogParameter().getParameterName();
					String value = params.get(key);
					if (StringUtils.isEmpty(value)) {
						value = "";
					}
					logFormat.setValue(value);
				}
				// 按照格式输出
				StringBuilder singleLog = new StringBuilder();
				singleLog.append(systemKey);
				singleLog.append(logSeparate);
				singleLog.append(logKey);
				singleLog.append(logSeparate);
				for (Iterator<LogFormat> iterator = formats.iterator(); iterator.hasNext();) {
					LogFormat logFormat = iterator.next();
					singleLog.append(logFormat.getValue());
					if (iterator.hasNext()) {
						singleLog.append(logSeparate);
					}
				}
				result.add(singleLog.toString());
			}
			return result;
		}
		return result;
	}

	@Override
	public void saveFailureJob(String name, String dir) {
		LogJob job = new LogJob();
		job.setName(name);
		job.setDir(dir);
		job.setStatus(0);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dir", dir);
		List<LogJob> jobs = logJobDao.find(params);
		if (jobs != null && jobs.size() > 0) {
			for (LogJob j : jobs) {
				logJobDao.remove(j.getId());
			}
		}
		logJobDao.persist(job);
	}

	@Override
	public void saveSuccessJob(String name, String dir) {
		LogJob job = new LogJob();
		job.setName(name);
		job.setDir(dir);
		job.setStatus(1);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dir", dir);
		List<LogJob> jobs = logJobDao.find(params);
		if (jobs != null && jobs.size() > 0) {
			for (LogJob j : jobs) {
				logJobDao.remove(j.getId());
			}
		}
		logJobDao.persist(job);
	}

	@Override
	public boolean isSuccessful(String dir) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dir", dir);
		List<LogJob> jobs = logJobDao.find(params);
		if (jobs != null && jobs.size() > 0) {
			LogJob job = jobs.get(0);
			if (job.getStatus() == 1) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 解析url返回map
	 * 
	 * @param url
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-8-30
	 */
	private Map<String, String> getMap(String url) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		if (url == null || url.endsWith("?")) {
			return result;
		}
		String[] entries = url.trim().split("[?]");
		if (entries.length != 2) {
			return result;
		}
		String[] params = entries[1].substring(0, entries[1].length() - 10).split("&");
		for (String param : params) {
			if (param.contains("=")) {
				String[] tmp = param.split("=");
				if (tmp.length == 1) {
					String key = java.net.URLDecoder.decode(tmp[0], "UTF-8");
					String value = "";
					result.put(key, value);
				} else if (tmp.length == 2) {
					String key = java.net.URLDecoder.decode(tmp[0], "UTF-8");
					String value = java.net.URLDecoder.decode(tmp[1], "UTF-8");
					result.put(key, value);
				}
			}
		}
		return result;
	}
}
