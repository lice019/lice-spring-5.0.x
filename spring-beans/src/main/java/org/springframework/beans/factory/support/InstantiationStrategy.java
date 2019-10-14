
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.Nullable;

/**
 * InstantiationStrategy：Spring的的根据bean的构造函数的实例化策略接口
 *
 * @since 1.1
 */
public interface InstantiationStrategy {

	/*
	 * 给定一个RootBeanDefinition，bean的名称、bean工厂来实例化一个bean的实例，返回Object类型
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner)
			throws BeansException;

	/*
	 *通过给定RootBeanDefinition，bean的名称、bean工厂来实例化一个bean的实例，返回Object类型
	 * 和bean的相应的构造器类和构造器相应的参数来实例化bean
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
					   Constructor<?> ctor, @Nullable Object... args) throws BeansException;

	/*
	 *
	 */
	Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
					   @Nullable Object factoryBean, Method factoryMethod, @Nullable Object... args)
			throws BeansException;

}
