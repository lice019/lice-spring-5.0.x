package org.springframework.beans.factory;

/**
 *
 *  Spring 中提供一些Aware相关接口， 比如 BeanFactoryAware、 ApplicationContextAware、 ResourceLoaderAware、 ServletContextAware 等，
 *  实现这些 Aware 接口的 bean 在被初始之后， 可以取得一些相对应的资源， 例如实现 BeanFactoryAware 的bean在初始后， Spring容器将会注入 BeanFactory的实例，
 *  而实现ApplicationContextAware的bean， 在bean被初始后， 将会被注入ApplicationContext的实例等。
 *
 * @since 3.1
 */
//标记接口
public interface Aware {

}
