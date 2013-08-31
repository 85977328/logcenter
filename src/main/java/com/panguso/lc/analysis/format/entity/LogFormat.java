/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
 * The persistent class for the log_format database table.
 * @author piaohailin
 * @date 2013-4-9
 */
@Entity
@Table(name = "log_format")
public class LogFormat implements Serializable, Comparable<LogFormat> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private int index;

	private int logId;

	private String description;

	@Transient
	private String value = "";

	@OneToOne
	@JoinColumn(name = "parameterId", referencedColumnName = "id")
	private LogParameter logParameter;

	@Override
	public int compareTo(LogFormat o) {
		if (this.index > o.getIndex()) {
			return 1;
		}
		return 0;
	}

	/**
	 * 
	 */
	public LogFormat() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getLogId() {
		return this.logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	/**
	 * @return the logParameter
	 */
	public LogParameter getLogParameter() {
		return logParameter;
	}

	/**
	 * @param logParameter
	 *        the logParameter to set
	 */
	public void setLogParameter(LogParameter logParameter) {
		this.logParameter = logParameter;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *        the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}