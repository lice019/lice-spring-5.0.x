
package org.springframework.beans.factory.config;

import org.springframework.lang.Nullable;

/**
 * SingletonBeanRegistry：对单例bean的管理接口规范
 * 为共享bean实例定义注册表的接口。
 * 可以通过{@linkorg.springframework.bean .factory实现。为了以统一的方式公开它们的单例管理功能。
 *
 * <p>The {@link ConfigurableBeanFactory} interface extends this interface.
 *
 * @see ConfigurableBeanFactory
 * @see org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 * @since 2.0
 */
public interface SingletonBeanRegistry {


	void registerSingleton(String beanName, Object singletonObject);


	@Nullable
	Object getSingleton(String beanName);


	boolean containsSingleton(String beanName);


	String[] getSingletonNames();


	int getSingletonCount();


	Object getSingletonMutex();

}
