package org.springframework.web.context;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.Aware;


//在Spring中，凡是实现ServletContextAware接口的类，都可以取得ServletContext
public interface ServletContextAware extends Aware {

	//给web上下文设置ServletContext
	void setServletContext(ServletContext servletContext);

}
