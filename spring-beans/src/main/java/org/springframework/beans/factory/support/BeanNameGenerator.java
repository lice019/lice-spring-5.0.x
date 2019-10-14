package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Strategy interface for generating bean names for bean definitions.
 * 用于为bean定义生成bean名称的策略接口
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 */
//BeanNameGenerator：生成bean名称的策略接口
public interface BeanNameGenerator {

	/**
	 * @param definition 要为其生成名称的bean定义
	 * @param registry   指定定义的bean定义注册表，bean应该在什么地方注册
	 * @return 返回bean的名称
	 */
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
