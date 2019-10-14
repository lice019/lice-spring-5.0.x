package org.springframework.web.context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;

/**
 * 由可配置的web应用程序上下文实现的接口。
 * 由{@link ContextLoader}和{@link org.springframework.web.servlet.FrameworkServlet}支持。
 *
 * @see ContextLoader#createWebApplicationContext
 * @see org.springframework.web.servlet.FrameworkServlet#createWebApplicationContext
 */
public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	/**
	 * Prefix for ApplicationContext ids that refer to context path and/or servlet name.
	 */
	String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";

	/**
	 * 工厂中ServletConfig环境bean的名称。
	 *
	 * @see javax.servlet.ServletConfig
	 */
	String SERVLET_CONFIG_BEAN_NAME = "servletConfig";


	/**
	 * Set the ServletContext for this web application context.
	 * <p>Does not cause an initialization of the context: refresh needs to be
	 * called after the setting of all configuration properties.
	 *
	 * @see #refresh()
	 */
	void setServletContext(@Nullable ServletContext servletContext);

	/**
	 * 为这个web应用程序上下文设置ServletConfig。
	 * 只调用属于特定Servlet的WebApplicationContext。
	 *
	 * @see #refresh()
	 */
	void setServletConfig(@Nullable ServletConfig servletConfig);

	/**
	 * Return the ServletConfig for this web application context, if any.
	 */
	@Nullable
	ServletConfig getServletConfig();

	/**
	 * 设置此web应用程序上下文的名称空间，用于构建缺省上下文配置位置。
	 * 根web应用程序上下文没有名称空间。
	 */
	void setNamespace(@Nullable String namespace);

	/**
	 * 返回此web应用程序上下文的名称空间(如果有的话)。
	 */
	@Nullable
	String getNamespace();

	//设置程序上下文的配置位置
	void setConfigLocation(String configLocation);

	/**
	 * 设置此web应用程序上下文的配置位置。
	 * 如果没有设置，则实现应该使用默认值
	 * 给定名称空间或根web应用程序上下文(视情况而定)。
	 */
	void setConfigLocations(String... configLocations);

	/**
	 * 返回此web应用程序上下文的配置位置，如果没有指定配置位置，则返回{@code null}。
	 */
	@Nullable
	String[] getConfigLocations();

}
