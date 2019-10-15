package com.lice;

import com.lice.config.AppConfig;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * description: MyWebApplicationInitialize <br>
 * date: 2019/10/15 15:08 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class MyWebApplicationInitialize implements WebApplicationInitializer {


	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {

		//初始化spring web容器
		AnnotationConfigWebApplicationContext acWeb = new AnnotationConfigWebApplicationContext();
		//注册配置给spring web容器
		acWeb.register(AppConfig.class);
		//刷新容器
		acWeb.refresh();

		//初始化Spring mvc的前端控制器DispatchServlet
		DispatcherServlet dispatcherServlet = new DispatcherServlet(acWeb);
		ServletRegistration.Dynamic registration = servletContext.addServlet("app", dispatcherServlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("*.do");


	}
}
/*

 以上实现就是之前版本使用web.xml的配置，spring boot也是这样从spring mvc来过渡的。
<web-app>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/app-context.xml</param-value>
    </context-param>

    <servlet>
        <servlet-name>app</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value></param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>app</servlet-name>
        <url-pattern>/app/*</url-pattern>
    </servlet-mapping>

</web-app>
 */
