
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Spring统一处理bean销毁方法
 *
 * @since 1.0.1
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {


	void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException;


	default boolean requiresDestruction(Object bean) {
		return true;
	}

}
