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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * 自定义输出格式
 * 
 * @author piaohailin
 * 
 * @param <K>
 * @param <V>
 */
public class TextOutputFormat<K, V> extends FileOutputFormat<K, V> {
	/**
	 * 改造部分
	 */
	private static final String separate = "";

	/**
	 * 
	 * @param <K>
	 * @param <V>
	 * @author piaohailin
	 * @date 2013-4-9
	 */
	protected static class LineRecordWriter<K, V> extends RecordWriter<K, V> {
		private static final String utf8 = "UTF-8";
		private static final byte[] newline;
		static {
			try {
				newline = "\n".getBytes(utf8);
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalArgumentException("can't find " + utf8 + " encoding");
			}
		}

		protected DataOutputStream out;
		private final byte[] keyValueSeparator;

		/**
		 * 
		 * @param out out
		 * @param keyValueSeparator keyValueSeparator
		 */
		public LineRecordWriter(DataOutputStream out, String keyValueSeparator) {
			this.out = out;
			try {
				this.keyValueSeparator = keyValueSeparator.getBytes(utf8);
			} catch (UnsupportedEncodingException uee) {
				throw new IllegalArgumentException("can't find " + utf8 + " encoding");
			}
		}

		/**
		 * 
		 * @param out out
		 */
		public LineRecordWriter(DataOutputStream out) {
			this(out, separate);
		}

		/**
		 * Write the object to the byte stream, handling Text as a special case.
		 * 
		 * @param o
		 *        the object to print
		 * @throws IOException
		 *         if the write throws, we pass it on
		 */
		private void writeObject(Object o) throws IOException {
			if (o instanceof Text) {
				Text to = (Text) o;
				out.write(to.getBytes(), 0, to.getLength());
			} else {
				out.write(o.toString().getBytes(utf8));
			}
		}

		/**
		 * 
		 * @param key key
		 * @param value value
		 * @throws IOException IOException
		 */
		public synchronized void write(K key, V value) throws IOException {

			boolean nullKey = key == null || key instanceof NullWritable;
			boolean nullValue = value == null || value instanceof NullWritable;
			if (nullKey && nullValue) {
				return;
			}
			if (!nullKey) {
				writeObject(key);
			}
			if (!(nullKey || nullValue)) {
				out.write(keyValueSeparator);
			}
			if (!nullValue) {
				writeObject(value);
			}
			out.write(newline);
		}

		/**
		 * @param context context
		 * @throws IOException IOException
		 */
		public synchronized void close(TaskAttemptContext context) throws IOException {
			out.close();
		}
	}

	/**
	 * @param job job
	 * @throws IOException IOException
	 * @throws InterruptedException InterruptedException
	 */
	public RecordWriter<K, V> getRecordWriter(TaskAttemptContext job) throws IOException,
	        InterruptedException {
		Configuration conf = job.getConfiguration();
		boolean isCompressed = getCompressOutput(job);
		String keyValueSeparator = conf.get("mapred.textoutputformat.separator", separate);
		CompressionCodec codec = null;
		String extension = "";
		if (isCompressed) {
			Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(job,
			        GzipCodec.class);
			codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
			extension = codec.getDefaultExtension();
		}
		Path file = getDefaultWorkFile(job, extension);
		FileSystem fs = file.getFileSystem(conf);
		if (!isCompressed) {
			FSDataOutputStream fileOut = fs.create(file, false);
			return new LineRecordWriter<K, V>(fileOut, keyValueSeparator);
		} else {
			FSDataOutputStream fileOut = fs.create(file, false);
			return new LineRecordWriter<K, V>(new DataOutputStream(
			        codec.createOutputStream(fileOut)), keyValueSeparator);
		}
	}
}
