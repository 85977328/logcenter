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

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * 
 * 
 * @author piaohailin
 * @date 2013-4-9
 */
public class FormatAnalysisReducer extends Reducer<Text, Text, Text, Text> {
	private static final Text blank = new Text();

	/**
	 * 
	 * @param key key
	 * @param values values
	 * @param context context
	 * @throws IOException IOException
	 * @throws InterruptedException InterruptedException
	 * @author piaohailin
	 * @date 2013-4-9
	 */
	public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
	        InterruptedException {
		context.write(key, blank);
	}
}
