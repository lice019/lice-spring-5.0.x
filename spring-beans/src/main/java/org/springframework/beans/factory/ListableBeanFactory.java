
package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 由bean工厂实现的{@link BeanFactory}接口的扩展，这些bean工厂可以枚举它们的所有bean实例，而不是根据客户机的请求逐个尝试bean查找。
 * 预加载所有bean定义(如基于xml的工厂)的BeanFactory实现可以实现此接口。
 *
 * ListableBeanFactory： 根 据 各 种 条 件 获 取 bean 的 配 置 清 单。
 *
 *
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 */
public interface ListableBeanFactory extends BeanFactory {

	//通过beanName判断bean容器中是否包含此名称的bean实例
	boolean containsBeanDefinition(String beanName);

	//返回工厂中定义的bean数量。
	int getBeanDefinitionCount();

	//返回该工厂中定义的所有bean的名称。
	String[] getBeanDefinitionNames();

	//根据bean定义或factorybean中{@code getObjectType}的值判断，返回与给定类型(包括子类)匹配的bean的名称。
	String[] getBeanNamesForType(ResolvableType type);

	//根据bean定义或factorybean中{@code getObjectType}的值判断，返回与给定类型(包括子类)匹配的bean的名称。
	String[] getBeanNamesForType(@Nullable Class<?> type);

	//此方法仅内省顶级bean。它是而不是
	//检查可能匹配指定类型的嵌套bean。
	//确实考虑了factorybean创建的对象，如果设置了“allowEagerInit”标志，这意味着factorybean将被初始化。如果FactoryBean创建的对象不匹配，原始的FactoryBean本身将根据类型进行匹配。如果没有设置“allowEagerInit”，那么只检查原始的FactoryBean(不需要初始化每个FactoryBean)。
	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);


	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;


	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	//查找所有使用提供的{@link Annotation}类型进行注释的bean的名称，而不需要创建相应的bean实例。
	//注意，这个方法会考虑factorybean创建的对象，这意味着为了确定它们的对象类型，factorybean将被初始化。
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);


	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	//在指定的bean上查找{@code annotationType}的{@link注释}，如果在给定的类本身上找不到注释，则遍历它的接口和超类。
	@Nullable
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

}
