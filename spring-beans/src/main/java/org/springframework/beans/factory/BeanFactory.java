package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * BeanFactory：是spring中bean 工厂的根接口，spring中所有的beanFactory都直接或间接的继承该接口。主要定义了一些规范的接口方法
 */
public interface BeanFactory {

	//用于取消对{@link FactoryBean}实例的引用，并将其与FactoryBean创建的bean 区别开来。
	//例如，如果名为{@code myJndiObject}的bean是FactoryBean，那么获取{@code &myJndiObject}将返回工厂，而不是工厂返回的实例。
	String FACTORY_BEAN_PREFIX = "&";


	//根据bean名称，获取bean实例
	Object getBean(String name) throws BeansException;

	//返回指定bean的一个实例，该实例可以是共享的，也可以是独立的。
	//的行为与{@link #getBean(String)}相同，但是如果bean不是必需的类型，则通过抛出BeanNotOfRequiredTypeException来提供类型安全性的度量。
	<T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;

	//根据多个参数来获取bean实例
	Object getBean(String name, Object... args) throws BeansException;

	<T> T getBean(Class<T> requiredType) throws BeansException;

	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

	//判断spring的IOC中是否含有name名称的bean实例
	boolean containsBean(String name);

	//bean实例是否是单例的
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	//bean实例是否是多例的
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	//根据bean的名称和类型，判断该bean是否匹配类型
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

	boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

	//确定具有给定名称的bean的类型。更具体地说，确定{@link #getBean}为给定名称返回的对象类型。
	@Nullable
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	//根据bean的名称，获取bean的别名
	String[] getAliases(String name);

}
