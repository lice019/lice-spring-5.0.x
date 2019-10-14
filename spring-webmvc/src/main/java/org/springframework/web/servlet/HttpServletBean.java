package org.springframework.web.servlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * HttpServletBean：继承了HttpServlet，对HttpServlet进行扩展。
 */
@SuppressWarnings("serial")
public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware {


	protected final Log logger = LogFactory.getLog(getClass());

	//可配置环境类，可以获取到web和spring环境的相关配置信息
	@Nullable
	private ConfigurableEnvironment environment;

	//将必须属性存储在Set集合中，需要时方便取
	private final Set<String> requiredProperties = new HashSet<>(4);


	//添加必须属性到Set集合中
	//使用了protected修饰，子类可以对其进行重写或扩展
	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}


	@Override
	public void setEnvironment(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "ConfigurableEnvironment required");
		this.environment = (ConfigurableEnvironment) environment;
	}

	//返回当前Servlet所依赖的环境对象
	@Override
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			//如果为null，创建一个StandardServletEnvironment
			this.environment = createEnvironment();
		}
		return this.environment;
	}

	//可由子类拓展和修改
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}


	//将配置参数映射到此servlet的bean属性，并调用子类初始化。
	@Override
	public final void init() throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		// Set bean properties from init parameters.
		//将初始化的参数设置到Servlet bean中
		//getServletConfig()--->ServletConfig
		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
		if (!pvs.isEmpty()) {
			try {
				//包装bean
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
				//资源加载器
				ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
				bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
				//初始化包装后的bean
				initBeanWrapper(bw);
				bw.setPropertyValues(pvs, true);
			} catch (BeansException ex) {
				if (logger.isErrorEnabled()) {
					logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
				}
				throw ex;
			}
		}

		// Let subclasses do whatever initialization they like.
		//让子类执行它们喜欢的任何初始化。
		initServletBean();

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}


	//为HttpServletBean初始化一个BeanWrapper
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {
	}


	protected void initServletBean() throws ServletException {
	}


	@Override
	@Nullable
	public String getServletName() {
		return (getServletConfig() != null ? getServletConfig().getServletName() : null);
	}


	/**
	 *由ServletConfig init参数创建的PropertyValues实现。
	 */
	private static class ServletConfigPropertyValues extends MutablePropertyValues {

		/**
		 * Create new ServletConfigPropertyValues.
		 *
		 * @param config             ServletConfig we'll use to take PropertyValues from
		 * @param requiredProperties set of property names we need, where
		 *                           we can't accept default values
		 * @throws ServletException if any required properties are missing
		 */
		public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties)
				throws ServletException {

			Set<String> missingProps = (!CollectionUtils.isEmpty(requiredProperties) ?
					new HashSet<>(requiredProperties) : null);

			Enumeration<String> paramNames = config.getInitParameterNames();
			while (paramNames.hasMoreElements()) {
				String property = paramNames.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// Fail if we are still missing properties.
			if (!CollectionUtils.isEmpty(missingProps)) {
				throw new ServletException(
						"Initialization from ServletConfig for servlet '" + config.getServletName() +
								"' failed; the following required properties were missing: " +
								StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
