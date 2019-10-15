package org.springframework.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * WebApplicationInitializer：是Spring的Web MVC模块用于初始化web容器的接口，该接口的实现根据Servlet3.0规范。
 * 实现零XML配置的web Servlet容器的启动。
 * 由于Tomcat和Spring是两个不同的产品的规范，tomcat不会去内嵌spring，所以spring想要做到像Servlet3.0那样做到零XML配置
 * 则必须实现Servlet中ServletContainerInitializer接口，Spring采取了SPI的技术，通过定义WebApplicationInitializer
 * 接口进行规范（来初始化spring web容器和DispatchServlet）。然后让org.springframework.web.SpringServletContainerInitializer实现ServletContainerInitializer
 * 接口，在Servlet容器启动时，立即加载ServletContainerInitializer所有实现类来实现Servlet容器的监听。
 *
 * @see SpringServletContainerInitializer
 * @see org.springframework.web.context.AbstractContextLoaderInitializer
 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer
 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer
 */
/*
WebApplicationInitializer是Spring MVC提供的接口，
可确保检测到您的实现并将其自动用于初始化任何Servlet 3容器。
通过WebApplicationInitializer命名方法 的抽象基类实现，
AbstractDispatcherServletInitializer可以DispatcherServlet通过重写方法
来指定servlet映射和DispatcherServlet配置位置，从而更加轻松地进行注册 。
 */
public interface WebApplicationInitializer {


	void onStartup(ServletContext servletContext) throws ServletException;

}
