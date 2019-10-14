
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

/**
 * BeanDefinitionRegistry：BeanDefinitionRegistry接口继承了AliasRegistry,在AliasRegistry的基础上,增加了对BeanDefinition的各种增删改查的操作。
 * 这7个方法都是用来操作容器内的BeanDefinition的。
 * 定 义 对 BeanDefinition 的 各 种 增 删 改 操 作。
 *
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
 */
public interface BeanDefinitionRegistry extends AliasRegistry {


	//将beanDefinition注册为指定的beanName
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	//移除指定名称的BeanDefinition
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//获得指定名称的BeanDefinition
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//判断是否包含指定名称的BeanDefinition
	boolean containsBeanDefinition(String beanName);

	//获得所有BeanDefinition的名称
	String[] getBeanDefinitionNames();

	//获得BeanDefinition的数量
	int getBeanDefinitionCount();

	//判断指定名称是否已经存在
	boolean isBeanNameInUse(String beanName);

}
