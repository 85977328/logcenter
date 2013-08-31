/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作，本实现中分页编号全部为 0 - N
 * 
 * @param <T>
 * @author piaohailin01
 */
public interface IDao<T> {

	/**
	 * 
	 * 查询条件当等的数据
	 * 
	 * @param params params
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-8-31
	 */
	List<T> find(Map<String, Object> params);

	/**
	 * 查询全部数据,不分页
	 * 
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-5-10
	 */
	List<T> find();

	/**
	 * 返回总记录数
	 * 
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-7-16
	 */
	long findCount();

	/**
	 * 查询全部数据,分页
	 * 
	 * @param pageNo pageNo
	 * @param pageSize pageSize
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-7-16
	 */
	List<T> find(int pageNo, int pageSize);

	/**
	 * 根据ID查询单条记录
	 * 
	 * @param primaryKey primaryKey
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-5-10
	 */
	T findByPrimaryKey(Serializable primaryKey);

	/**
	 * 持久化实体,如果存在重复,则抛出异常
	 * 
	 * @param entity entity
	 * 
	 * @author piaohailin
	 * @date 2012-5-10
	 */
	void persist(T entity);

	/**
	 * 持久化实体，如果存在重复,则替换;如果不存在重复,则新增
	 * 
	 * @param entity entity
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-5-10
	 */
	T merge(T entity);

	/**
	 * 根据主键删除对象
	 * 
	 * @param primaryKey primaryKey
	 * 
	 * @author piaohailin
	 * @date 2012-5-10
	 */
	void remove(Serializable primaryKey);

	/**
	 * 搜索,and条件
	 * 
	 * @param condition condition
	 * @param pageNo pageNo
	 * @param pageSize pageSize
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-7-19
	 */
	List<T> search(T condition, int pageNo, int pageSize);

	/**
	 * 搜索,and条件
	 * 
	 * @param condition condition
	 * 
	 * @return
	 * 
	 * @author piaohailin
	 * @date 2012-7-19
	 */
	long searchCount(T condition);
}