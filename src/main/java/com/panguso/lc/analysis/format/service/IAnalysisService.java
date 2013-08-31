/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.service;

import java.util.List;
import java.util.Map;

import com.panguso.lc.analysis.format.entity.LogFormat;

/**
 * @author piaohailin
 * @date 2012-8-30
 */
public interface IAnalysisService {

	/**
	 * 验证结构库中，单条记录是否存在
	 * 
	 * @param systemKey systemKey
	 * @param logKey logKey
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-9-3
	 */
	boolean checkRecord(String systemKey, String logKey);

	/**
	 * 取得单条格式
	 * 
	 * @param logKey logKey
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-9-3
	 */
	List<LogFormat> getFormat(String logKey);

	/**
	 * 设置单条记录的格式
	 * 
	 * @param params params
	 * @param formats formats
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-8-31
	 */
	void setValue(Map<String, String> params, List<LogFormat> formats);

	/**
	 * 取得格式化后的字符串
	 * 
	 * @param line line
	 * @return
	 * 
	 * @throws Exception Exception
	 * @author piaohailin
	 * @date 2012-9-3
	 */
	List<String> getFormatedString(String line) throws Exception;

	/**
	 * 保存失败的任务目录
	 * 
	 * @param name name
	 * @param dir dir
	 * 
	 * @author piaohailin
	 * @date 2012-9-4
	 */
	void saveFailureJob(String name, String dir);

	/**
	 * 
	 * @param name name
	 * @param dir dir
	 * 
	 * @author piaohailin
	 * @date 2012-10-31
	 */
	void saveSuccessJob(String name, String dir);

	/**
	 * 解析日志是否解析成功过
	 * 
	 * @param dir dir
	 * @author piaohailin
	 * @date 2012-10-31
	 */
	boolean isSuccessful(String dir);
}
