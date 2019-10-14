
package org.springframework.beans.factory;

/**
 * Spring对bean的统一销毁的接口
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 * @see InitializingBean
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName()
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
 * @see org.springframework.context.ConfigurableApplicationContext#close()
 */
public interface DisposableBean {

	/*
	 * 对bean进行销毁
	 */
	void destroy() throws Exception;

}
