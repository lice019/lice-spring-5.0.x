package org.springframework.web.context.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

import java.io.IOException;

/**
 * web.xml
 * 在spring web应用项目中的web.xml配置文件：
 * spring监听器：
 * <!--ContextLoaderListener的作用就是启动Web容器时，自动装配ApplicationContext的配置信息。因为它实现了ServletContextListener这个接口，在web.xml配置这个监听器，启动容器时，就会默认执行它实现的方法。
 * <listener>
 * <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
 * </listener>
 * spring的配置文件：ApplicationContext.xml
 * <context-param>
 * <param-name>contextConfigLocation</param-name>
 * <param-value>classpath:spring/applicationContext.xml</param-value>
 * </context-param>
 * spring mvc的核心控制器：DispatchServlet：
 * <servlet>
 * <servlet-name>DispatcherServlet</servlet-name><!--在DispatcherServlet的初始化过程中，框架会在web应用的 WEB-INF文件夹下寻找名为[servlet-name]-servlet.xml 的配置文件，生成文件中定义的bean。-->
 * <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
 * <init-param>
 * <param-name>contextConfigLocation</param-name>
 * <param-value>classpath:spring/dispatcher-servlet.xml</param-value>
 * </init-param>
 * <load-on-startup>1</load-on-startup><!--是启动顺序，让这个Servlet随Servletp容器一起启动。-->
 * </servlet>
 * <servlet-mapping>
 * <servlet-name>DispatcherServlet</servlet-name>
 * <url-pattern>/</url-pattern> <!--会拦截URL中带“/”的请求。-->
 * </servlet-mapping>
 */

public class XmlWebApplicationContext extends AbstractRefreshableWebApplicationContext {

	//根上下文的默认配置位置,即spring中ApplicationContext.xml
	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";

	//配置文件路径的默认前缀
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";

	//配置文件路径的默认后缀
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";


	/**
	 * 通过XmlBeanDefinitionReader加载bean定义。
	 *
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		//为给定的bean工厂创建一个新的XmlBeanDefinitionReader。
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		//使用此上下文的资源加载环境配置bean定义阅读器。
		beanDefinitionReader.setEnvironment(getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		//允许子类提供阅读器的自定义初始化，然后继续实际加载bean定义。
		initBeanDefinitionReader(beanDefinitionReader);
		//将子类的提供的BeanDefinition读取器去读取配置路径，将配置路径装配成一个bean实例，并注册到IOC容器中
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * 初始化用于加载bean的bean定义读取器
	 * 此上下文的定义。默认实现为空。
	 * 可以在子类中重写，例如用于关闭XML验证
	 * 或者使用不同的XmlBeanDefinitionParser实现。
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
	}

	//将配置路径装配成bean
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
		//获取配置路径，是一个数组
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			for (String configLocation : configLocations) {
				//将配置每一个配置路径加载成bean实例，存储到IOC，供spring框架使用
				reader.loadBeanDefinitions(configLocation);
			}
		}
	}

	//获取默认的配置路径信息
	@Override
	protected String[] getDefaultConfigLocations() {
		if (getNamespace() != null) {
			return new String[]{DEFAULT_CONFIG_LOCATION_PREFIX + getNamespace() + DEFAULT_CONFIG_LOCATION_SUFFIX};
		} else {
			return new String[]{DEFAULT_CONFIG_LOCATION};
		}
	}

}
