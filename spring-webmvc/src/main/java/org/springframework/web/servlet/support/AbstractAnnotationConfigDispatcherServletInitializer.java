package org.springframework.web.servlet.support;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * AbstractAnnotationConfigDispatcherServletInitializer：用于Java配置，但又不借助WebApplicationContext
 * 来初始web容器，实际上是反顺序来初始化web容器，通过初始化DispatchServlet后，再初始化WebApplicationContext
 *
 * 作用：给开发人员通过继续该类来实现Java配置的web容器初始化
 *
 * @since 3.2
 */
public abstract class AbstractAnnotationConfigDispatcherServletInitializer
		extends AbstractDispatcherServletInitializer {


	//这个实现方法创建一个{@link AnnotationConfigWebApplicationContext},可以通过{@link #getRootConfigClasses()}.返回注解配置类
	@Override
	@Nullable
	protected WebApplicationContext createRootApplicationContext() {
		//获取注解配置类
		Class<?>[] configClasses = getRootConfigClasses();
		if (!ObjectUtils.isEmpty(configClasses)) {
			//通过注解web配置应用上下文注册java方式的配置类，并返回上下文AnnotationConfigWebApplicationContext
			AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
			context.register(configClasses);
			//返回AnnotationConfigWebApplicationContext
			return context;
		} else {
			return null;
		}
	}

	//创建一个{@link AnnotationConfigWebApplicationContext}，返回一个Servlet的配置类
	@Override
	protected WebApplicationContext createServletApplicationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		//获取Servlet的配置类
		Class<?>[] configClasses = getServletConfigClasses();
		if (!ObjectUtils.isEmpty(configClasses)) {
			//注册Servlet配置bean
			context.register(configClasses);
		}
		//返回上下文
		return context;
	}


	//由子类实现，指定JavaBean的配置（RootConfig.class、WebConfig.class）
	//spring配置的指定
	@Nullable
	protected abstract Class<?>[] getRootConfigClasses();

	//spring-mvc的配置类指定
	@Nullable
	protected abstract Class<?>[] getServletConfigClasses();

}

/*
 * public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { RootConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { App1Config.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/app1/*" };
    }
}
* 等价于web.xml：
<web-app>

    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/root-context.xml</param-value>
    </context-param>

    <servlet>
        <servlet-name>app1</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/app1-context.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>app1</servlet-name>
        <url-pattern>/app1/*</url-pattern>
    </servlet-mapping>

</web-app>
 **/