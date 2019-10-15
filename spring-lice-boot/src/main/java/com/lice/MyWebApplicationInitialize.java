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

		//初始化spring容器
		AnnotationConfigWebApplicationContext acWeb = new AnnotationConfigWebApplicationContext();
		//注册配置给spring容器
		acWeb.register(AppConfig.class);
		//刷新容器
		acWeb.refresh();

		//初始化Spring mvc的前端控制器DispatchServlet
		DispatcherServlet dispatcherServlet = new DispatcherServlet(acWeb);
		ServletRegistration.Dynamic registration = servletContext.addServlet("app",dispatcherServlet);
		registration.setLoadOnStartup(1);
		registration.addMapping("*.do");



	}
}
