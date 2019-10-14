
package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * 一般情况下， Spring通过反射机制利用bean的class属性指定实现类来实例化bean 。在某些情况下，实例化bean 过程比较复杂，如果按照传统的方式，
 * 则需要在<bean>中提供大量的配置信息，配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。
 * Spring为此提供了一个org.Springframework.bean.factory.FactoryBean 的工厂类接口，用户可以通过实现该接口定制实例化bean的逻辑。
 * FactoryBean 接口对于Spring框架来说占有重要的地位， Spring 自身就提供了70多个FactoryBean 的实现。
 * 它们隐藏了实例化一些复杂bean 的细节， 给上层应用带来了便利。
 *
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public interface FactoryBean<T> {

	/*
	 *返回由FactoryBean创建的bean实例，如果isSingleton()返回true，则实例会被放到Spring容器的单例缓存池中
	 */
	/*
	 *当配置文件中<bean>的class属性配置的实现类是FactoryBean时，通过getBean()方法返回的不是FactoryBean本身，
	 * 而是FactoryBean#getObject()方法所返回的对象，相当于FactoryBean#getObject() 代理了getBean()方法。
	 */
	@Nullable
	T getObject() throws Exception;


	//返回由FactoryBean创建的bean类型
	@Nullable
	Class<?> getObjectType();

	/*
	 *返回由FactoryBean创建的bean实例的作用域是Singleton还是Prototype
	 */
	default boolean isSingleton() {
		return true;
	}

}
