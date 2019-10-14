package org.springframework.web.context.support;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;

/**
 * 将ServletContext传递给实现bean的实现。
 * <p>
 * Web应用程序上下文将自动注册这个底层bean工厂。应用程序不直接使用它。
 *
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @see org.springframework.web.context.ServletContextAware
 * @see org.springframework.web.context.support.XmlWebApplicationContext#postProcessBeanFactory
 * @since 12.03.2004
 */
public class ServletContextAwareProcessor implements BeanPostProcessor {

	//Servlet的上下文
	@Nullable
	private ServletContext servletContext;

	//Servlet的配置
	@Nullable
	private ServletConfig servletConfig;


	//初始化一个ServletContextAwareProcessor
	protected ServletContextAwareProcessor() {
	}

	/**
	 * 初始化一个含有ServletContext的ServletContextAwareProcessor实例
	 */
	public ServletContextAwareProcessor(ServletContext servletContext) {
		this(servletContext, null);
	}

	/**
	 * 初始化一个含有ServletConfig的ServletContextAwareProcessor实例
	 */
	public ServletContextAwareProcessor(ServletConfig servletConfig) {
		this(null, servletConfig);
	}

	/**
	 * 初始化一个含有ServletConfig和ServletContext的ServletContextAwareProcessor实例
	 */
	public ServletContextAwareProcessor(@Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig) {
		this.servletContext = servletContext;
		this.servletConfig = servletConfig;
	}


	//返回一个被注册到bean工厂的ServletContext bean
	@Nullable
	protected ServletContext getServletContext() {
		if (this.servletContext == null && getServletConfig() != null) {
			return getServletConfig().getServletContext();
		}
		return this.servletContext;
	}

	//返回一个被注册到bean工厂的ServletConfig bean
	@Nullable
	protected ServletConfig getServletConfig() {
		return this.servletConfig;
	}

	//初始化bean之前调用，做bean初始化的前置处理。
	//这个方法主要是给Servlet的bean调用，为Servlet bean做一些重要的属性设置
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		//判断传入的bean是否为ServletContextAware或ServletContextAware的实现类、子类的实例对象
		if (getServletContext() != null && bean instanceof ServletContextAware) {
			//给web上下文设置ServletContext
			((ServletContextAware) bean).setServletContext(getServletContext());
		}
		//给web上下文设置ServletConfig
		if (getServletConfig() != null && bean instanceof ServletConfigAware) {
			((ServletConfigAware) bean).setServletConfig(getServletConfig());
		}
		//返回一个含有ServletContext和ServletConfig的bean
		return bean;
	}

	//初始化bean之后调用，做bean初始化的后置处理
	//这里直接返回传入的bean
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

}
