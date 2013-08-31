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
import javax.persistence.Table;

/**
 * The persistent class for the log_record database table.
 * 
 * @author piaohailin
 * @date 2013-4-9
 */
@Entity
@Table(name = "log_record")
public class LogRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int               id;

    private int               logId;

    private int               systemId;

    /**
     * 
     */
    public LogRecord() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLogId() {
        return this.logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getSystemId() {
        return this.systemId;
    }

    public void setSystemId(int systemId) {
        this.systemId = systemId;
    }

}