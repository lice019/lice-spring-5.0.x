package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

//HierarchicalBeanFactory(分层beanFactory):提供父容器的访问功能
public interface HierarchicalBeanFactory extends BeanFactory {

	//  返回本Bean工厂的父工厂
	@Nullable
	BeanFactory getParentBeanFactory();

	// 本地工厂(容器)是否包含这个Bean
	boolean containsLocalBean(String name);

}
