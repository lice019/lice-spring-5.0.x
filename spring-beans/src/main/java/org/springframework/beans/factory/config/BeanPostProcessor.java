package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;


/**
 * BeanPostProcessors在spring中是一个非常重要的扩展接口,允许自定义修改新bean实例的工厂钩子，例如检查标记接口或用代理包装它们。
 * ApplicationContexts可以自动检测它们中的BeanPostProcessor bean bean定义.普通bean工厂允许编程注册后处理程序，适用于通过该工厂创建的所有bean。
 * 通常是通过标记接口填充bean的后处理器,或者类似的会实现{@link # postprocessbefore初始化}，
 * 而使用代理包装bean的后处理器通常会这实现{@link # postProcessAfterInitialization}。
 *
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */

/**
 * BeanPostProcessor接口作用是：如果我们需要在Spring容器完成Bean的实例化、配置和其他的初始化前后添加一些自己的逻辑处理（也就是用户足够的权限去扩展Spring），我们就可以定义一个或者多个BeanPostProcessor接口的实现，然后注册到容器中。
 */
public interface BeanPostProcessor {
	//初始化之前调用
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	//初始化之后调用
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
