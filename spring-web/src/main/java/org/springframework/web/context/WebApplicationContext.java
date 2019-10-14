package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;

/**
 * 为web应用程序提供配置的接口。在应用程序运行时这是只读的，但如果实现支持这一点，则可以重新加载。
 * WebApplicationContext：扩展了ApplicationContext的父接口。
 */
public interface WebApplicationContext extends ApplicationContext {

	/**
	 * 上下文属性，用于在成功启动时将根WebApplicationContext绑定到。
	 * 注意:如果根上下文的启动失败，这个属性可以包含作为值的异常或错误。使用WebApplicationContext tils方便地查找根WebApplicationContext。
	 *
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getRequiredWebApplicationContext
	 */
	String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class.getName() + ".ROOT";

	/**
	 * request请求的作用范围scope
	 */
	String SCOPE_REQUEST = "request";

	/**
	 * Session的作用范围scope
	 */
	String SCOPE_SESSION = "session";

	/**
	 * 全局web应用程序范围的范围标识符:“application”。
	 * 除了标准作用域“单例”和“原型”外，还支持。
	 */
	String SCOPE_APPLICATION = "application";

	/**
	 * 工厂中ServletContext环境bean的名称。
	 *
	 * @see javax.servlet.ServletContext
	 */
	String SERVLET_CONTEXT_BEAN_NAME = "servletContext";

	/**
	 * 工厂中的ServletContext/PortletContext init-params环境bean的名称。
	 * 注意:可能与ServletConfig/PortletConfig参数合并。
	 * ServletConfig参数覆盖同名的ServletContext参数。
	 *
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 * @see javax.servlet.ServletContext#getInitParameter(String)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 * @see javax.servlet.ServletConfig#getInitParameter(String)
	 */
	String CONTEXT_PARAMETERS_BEAN_NAME = "contextParameters";

	/**
	 * 工厂中的ServletContext/PortletContext属性环境bean的名称。
	 *
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 * @see javax.servlet.ServletContext#getAttribute(String)
	 */
	String CONTEXT_ATTRIBUTES_BEAN_NAME = "contextAttributes";


	/**
	 * 回此应用程序的标准Servlet API ServletContext。
	 */
	@Nullable
	ServletContext getServletContext();

}
