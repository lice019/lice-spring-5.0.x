package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 引导监听器启动和关闭Spring的root {@link WebApplicationContext}。
 * 简单地委托给{@link ContextLoader}和{@link ContextCleanupListener}。
 * 从Spring 3.1开始，{@code ContextLoaderListener}支持通过{@link #ContextLoaderListener(WebApplicationContext)}构造函数注入根web应用程序上下文，允许在Servlet 3.0+环境中进行编程配置。
 * 看到{@link org.springframework.web。用于使用示例的WebApplicationInitializer}。
 *
 * @see #setContextInitializers
 * @see org.springframework.web.WebApplicationInitializer
 */

/**
 * ContextLoaderListener：简单说，主要作用就是监听当web容器启动时创建WebApplicationContext对象（WebApplicationContext是ApplicationContext的子类）并且存放到ServletContext中。
 * 类似的等效代码 ：
 * ApplicationContext acc=new ClassPathXmlApplicationContext(“spring-context.xml”);
 * ContextLoaderListener继承了ContextLoader，并实现了ServletContextListener的接口规范(这里可以看出spring和Servlet的关系)
 */
//ContextLoaderListener：用于监听tomcat启动初始化IOC容器(即tomcat启动，自动装配ApplicationContext)
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

	//实例化ContextLoaderListener，实例化ContextLoaderListener前会先加载ContextLoader父类字节码，再调用父类ContextLoader的无参构造方法实例化父类。
	public ContextLoaderListener() {
	}

	//实例化ContextLoaderListener，传入WebApplicationContext
	public ContextLoaderListener(WebApplicationContext context) {
		super(context);
	}


	/**
	 * 初始化根web应用程序上下文。
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		initWebApplicationContext(event.getServletContext());
	}


	/**
	 * 关闭根web应用程序上下文。
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		closeWebApplicationContext(event.getServletContext());
		ContextCleanupListener.cleanupAttributes(event.getServletContext());
	}

}
