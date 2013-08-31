/**
 *  Copyright (c)  2011-2020 Panguso, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of Panguso, 
 *  Inc. ("Confidential Information"). You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into with Panguso.
 */
package com.panguso.lc.analysis.format.dao.impl;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.panguso.lc.analysis.format.dao.IDao;

/**
 * 数据库操作抽象基类
 * 
 * @author piaohailin01
 * 
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class DaoImpl<T> implements IDao<T> {
	protected Class<T> entityClass;

	@PersistenceContext
	private EntityManager em;

//	private JpaTemplate jpaTemplate;

	/**
	 * 构造方法W
	 */
	public DaoImpl() {
		ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
		this.entityClass = (Class<T>) type.getActualTypeArguments()[0];
	}

	@Override
	public List<T> find(Map<String, Object> params) {
		StringBuilder qString = new StringBuilder("select model from "
		        + entityClass.getSimpleName() + " model");
		if (params != null && params.size() > 0) {
			qString.append(" where ");
			Iterator<Entry<String, Object>> itr = params.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<String, Object> entry = itr.next();
				if ("java.lang.String".equals(entry.getValue().getClass().getName())) {
					qString.append("model.");
					qString.append(entry.getKey());
					qString.append("=");
					qString.append("'");
					qString.append(entry.getValue());
					qString.append("'");
				} else {
					qString.append("model.");
					qString.append(entry.getKey());
					qString.append("=");
					qString.append(entry.getValue());
				}

				if (itr.hasNext()) {
					qString.append(" and ");
				}
			}
		}

		Query query = em.createQuery(qString.toString());

		return query.getResultList();
	}

	@Override
	public List<T> find() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
		Root<T> customer = criteriaQuery.from(entityClass);
		criteriaQuery.getRoots().add(customer);
		Query query = em.createQuery(criteriaQuery);
		return query.getResultList();
	}

	@Override
	public long findCount() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
		Root<T> customer = criteriaQuery.from(entityClass);
		criteriaQuery.select(cb.count(customer));
		Query query = em.createQuery(criteriaQuery);
		return (Long) query.getSingleResult();
	}

	@Override
	public List<T> find(int pageNo, int pageSize) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
		Root<T> customer = criteriaQuery.from(entityClass);
		criteriaQuery.getRoots().add(customer);
		Query query = em.createQuery(criteriaQuery);
		query.setFirstResult(pageNo * pageSize);
		query.setMaxResults(pageSize);
		return query.getResultList();
	}

	/**
	 * 自定义SQL语句查询类
	 * @param sqlString sqlString
	 * @return
	 * @author piaohailin
	 * @date 2013-4-15
	 */
	protected List<T> find(String sqlString) {
		return em.createQuery(sqlString).getResultList();
	}

	@Override
	public T findByPrimaryKey(Serializable primaryKey) {
		return em.find(entityClass, primaryKey);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void persist(T entity) {
		em.persist(entity);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public T merge(T entity) {
		return em.merge(entity);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void remove(Serializable primaryKey) {
		T entity = em.find(entityClass, primaryKey);
		em.remove(entity);
	}

	@Override
	public List<T> search(T condition, int pageNo, int pageSize) {
		StringBuilder qString = new StringBuilder("select model from "
		        + entityClass.getSimpleName() + " model");
		StringBuilder qWhere = new StringBuilder(" where ");
		StringBuilder qCondition = new StringBuilder();
		PropertyDescriptor[] propertyDescriptors = PropertyUtils
		        .getPropertyDescriptors(entityClass);
		for (int i = 0, count = propertyDescriptors.length; i < count; i++) {
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			String name = propertyDescriptor.getName();
			Class<?> type = propertyDescriptor.getPropertyType();
			String value = null;
			try {
				value = BeanUtils.getProperty(condition, name);
			} catch (Exception e) {
				// 如果没有属性，此异常直接吞掉
				continue;
			}
			if (value == null || name.equals("class")) {
				continue;
			}
			if ("java.lang.String".equals(type.getName())) {
				qCondition.append("model.");
				qCondition.append(name);
				qCondition.append(" like ");
				qCondition.append("'%");
				qCondition.append(value);
				qCondition.append("%'");
			} else {
				qCondition.append("model.");
				qCondition.append(name);
				qCondition.append("=");
				qCondition.append(value);
			}
			qCondition.append(" and ");

		}
		if (qCondition.length() != 0) {
			qString.append(qWhere).append(qCondition);
			if (qCondition.toString().endsWith(" and ")) {
				qString.delete(qString.length() - " and ".length(), qString.length());
			}
		}
		Query query = em.createQuery(qString.toString());
		query.setFirstResult(pageNo * pageSize);
		query.setMaxResults(pageSize);
		return query.getResultList();
	}

	@Override
	public long searchCount(T condition) {
		StringBuilder qString = new StringBuilder("select count(model) from "
		        + entityClass.getSimpleName() + " model");
		StringBuilder qWhere = new StringBuilder(" where ");
		StringBuilder qCondition = new StringBuilder();
		PropertyDescriptor[] propertyDescriptors = PropertyUtils
		        .getPropertyDescriptors(entityClass);
		for (int i = 0, count = propertyDescriptors.length; i < count; i++) {
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			String name = propertyDescriptor.getName();
			Class<?> type = propertyDescriptor.getPropertyType();
			String value = null;
			try {
				value = BeanUtils.getProperty(condition, name);
			} catch (Exception e) {
				// 如果没有属性，此异常直接吞掉
				continue;
			}
			if (value == null || name.equals("class")) {
				continue;
			}
			if ("java.lang.String".equals(type.getName())) {
				qCondition.append("model.");
				qCondition.append(name);
				qCondition.append(" like ");
				qCondition.append("'%");
				qCondition.append(value);
				qCondition.append("%'");
			} else {
				qCondition.append("model.");
				qCondition.append(name);
				qCondition.append("=");
				qCondition.append(value);
			}
			qCondition.append(" and ");
		}
		if (qCondition.length() != 0) {
			qString.append(qWhere).append(qCondition);
			if (qCondition.toString().endsWith(" and ")) {
				qString.delete(qString.length() - " and ".length(), qString.length());
			}
		}
		Query query = em.createQuery(qString.toString());
		return (Long) query.getSingleResult();
	}
}