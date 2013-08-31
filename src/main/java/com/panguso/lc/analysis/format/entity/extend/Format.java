/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.entity.extend;

import java.util.List;

import com.panguso.lc.analysis.format.entity.LogInfo;
import com.panguso.lc.analysis.format.entity.LogParameter;
import com.panguso.lc.analysis.format.entity.LogSystem;

/**
 * @author piaohailin
 * @date 2012-8-30
 */
public class Format {
    private LogInfo            logInfo;
    private LogSystem          logSystem;
    private List<LogParameter> logParameters;

    /**
     * 返回格式化以后的日志
     */
    public String toString() {
        return "";
    }

    /**
     * @return the logInfo
     */
    public LogInfo getLogInfo() {
        return logInfo;
    }

    /**
     * @param logInfo
     *            the logInfo to set
     */
    public void setLogInfo(LogInfo logInfo) {
        this.logInfo = logInfo;
    }

    /**
     * @return the logSystem
     */
    public LogSystem getLogSystem() {
        return logSystem;
    }

    /**
     * @param logSystem
     *            the logSystem to set
     */
    public void setLogSystem(LogSystem logSystem) {
        this.logSystem = logSystem;
    }

    /**
     * @return the logParameters
     */
    public List<LogParameter> getLogParameters() {
        return logParameters;
    }

    /**
     * @param logParameters
     *            the logParameters to set
     */
    public void setLogParameters(List<LogParameter> logParameters) {
        this.logParameters = logParameters;
    }
}
