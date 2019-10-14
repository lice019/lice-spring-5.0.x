
package org.springframework.web.servlet.support;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.Conventions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

/**
 * AbstractDispatcherServletInitializer：抽象springmvc的前端控制器初始化基类，给子类通过初始化功能。
 * 在servlet上下文中注册 {@link org.springframework.web.WebApplicationInitializer}实现的基类，
 * 并注册{@link DispatcherServlet} 上下文Servlet。
 *
 * <p>大多数应用程序应该考虑扩展Spring Java配置子类
 * {@link AbstractAnnotationConfigDispatcherServletInitializer}.
 *
 * @author Arjen Poutsma
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.2
 */
public abstract class AbstractDispatcherServletInitializer extends AbstractContextLoaderInitializer {


	//默认的servlet名称。可以通过重写{@link #getServletName}进行自定义。
	public static final String DEFAULT_SERVLET_NAME = "dispatcher";


	//启动Servlet上文应用
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		//注册springmvc前端控制到IOC中
		registerDispatcherServlet(servletContext);
	}


	protected void registerDispatcherServlet(ServletContext servletContext) {
		//获取前端控制器DispatcherServlet的名称
		String servletName = getServletName();
		//进行断言
		Assert.hasLength(servletName, "getServletName() must not return null or empty");
		//初始化一个web容器
		WebApplicationContext servletAppContext = createServletApplicationContext();
		Assert.notNull(servletAppContext, "createServletApplicationContext() must not return null");

		//根据Servlet上下文，创建前端控制器DispatcherServlet
		FrameworkServlet dispatcherServlet = createDispatcherServlet(servletAppContext);
		Assert.notNull(dispatcherServlet, "createDispatcherServlet(WebApplicationContext) must not return null");
		//前端控制器注入一个ServletApplicationContextInitializers，进行web Servlet容器初始化
		dispatcherServlet.setContextInitializers(getServletApplicationContextInitializers());

		ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, dispatcherServlet);
		if (registration == null) {
			throw new IllegalStateException("Failed to register servlet with name '" + servletName + "'. " +
					"Check if there is another servlet registered under the same name.");
		}

		//设置启动级别
		registration.setLoadOnStartup(1);
		//添加映射
		registration.addMapping(getServletMappings());
		//设置异步支持
		registration.setAsyncSupported(isAsyncSupported());

		//获取Servlet的过滤器
		Filter[] filters = getServletFilters();
		if (!ObjectUtils.isEmpty(filters)) {
			for (Filter filter : filters) {
				registerServletFilter(servletContext, filter);
			}
		}

		customizeRegistration(registration);
	}


	protected String getServletName() {
		return DEFAULT_SERVLET_NAME;
	}


	protected abstract WebApplicationContext createServletApplicationContext();


	//创建前端控制器DispatcherServlet
	protected FrameworkServlet createDispatcherServlet(WebApplicationContext servletAppContext) {
		return new DispatcherServlet(servletAppContext);
	}


	@Nullable
	protected ApplicationContextInitializer<?>[] getServletApplicationContextInitializers() {
		return null;
	}

	/**
	 * Specify the servlet mapping(s) for the {@code DispatcherServlet} &mdash;
	 * for example {@code "/"}, {@code "/app"}, etc.
	 * @see #registerDispatcherServlet(ServletContext)
	 */
	protected abstract String[] getServletMappings();


	@Nullable
	protected Filter[] getServletFilters() {
		return null;
	}

	//注册Servlet过滤器
	protected FilterRegistration.Dynamic registerServletFilter(ServletContext servletContext, Filter filter) {
		String filterName = Conventions.getVariableName(filter);
		Dynamic registration = servletContext.addFilter(filterName, filter);

		if (registration == null) {
			int counter = 0;
			while (registration == null) {
				if (counter == 100) {
					throw new IllegalStateException("Failed to register filter with name '" + filterName + "'. " +
							"Check if there is another filter registered under the same name.");
				}
				registration = servletContext.addFilter(filterName + "#" + counter, filter);
				counter++;
			}
		}

		registration.setAsyncSupported(isAsyncSupported());
		registration.addMappingForServletNames(getDispatcherTypes(), false, getServletName());
		return registration;
	}

	private EnumSet<DispatcherType> getDispatcherTypes() {
		return (isAsyncSupported() ?
				EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ASYNC) :
				EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE));
	}


	protected boolean isAsyncSupported() {
		return true;
	}


	protected void customizeRegistration(ServletRegistration.Dynamic registration) {
	}

}
