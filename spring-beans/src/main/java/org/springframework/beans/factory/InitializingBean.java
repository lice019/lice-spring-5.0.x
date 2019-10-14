
package org.springframework.beans.factory;

/**
 * 客户定制的初始化方法除了我们熟知的使用配置init-method外，还有使自定义的bean实现InitializingBean接口，
 * 并在afterPropertiesSet中实现自己的初始化业务逻辑。
 *
 * @see DisposableBean
 * @see org.springframework.beans.factory.config.BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getInitMethodName()
 */
public interface InitializingBean {

	/*
	 * 在bean初始化之前实现自己逻辑代码
	 */
	void afterPropertiesSet() throws Exception;

}
