package org.springframework.web.servlet.support;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * AbstractAnnotationConfigDispatcherServletInitializer：用于java方式配置初始化一个web.xml 中配置Dispatcherservlet（spring mvc的前端控制器） 和 ContextConfigLocation（spring的监听器，用于tomcat启动初始化IOC容器） 和一些拦截器。
 * 在spring3.0之后，java配置方式完全可以取代了xml配置方式，包括web.xml的配置。
 *
 * <p>
 * {@link org.springframework.web.WebApplicationInitializer WebApplicationInitializer}
 * 去注册一个 {@code DispatcherServlet} 并使用基于Java的Spring配置。
 *
 * <p>实现需要实现:
 * <ul>
 * <li>{@link #getRootConfigClasses()} -- 用于“根”应用程序上下文（非Web基础结构）配置。
 * <li>{@link #getServletConfigClasses()} -- 用于 {@code DispatcherServlet}应用程序上下文（spring mvc基础结构）配置。
 * </ul>
 *
 * <p>I如果不需要应用程序上下文层次结构，则应用程序可以通过 {@link #getRootConfigClasses()} 并返回
 * {@code null} from {@link #getServletConfigClasses()}.
 *
 * @author Arjen Poutsma
 * @author Chris Beams
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

/**
 * @Override
 *        protected Class<?>[] getRootConfigClasses() {
 * 		return new Class<?>[] { RootConfig.class };
 *    }
 *
 *    @Override
 *    protected Class<?>[] getServletConfigClasses() {
 * 		return new Class<?>[] { WebConfig.class };        //指定Web配置类
 *    }
 *
 *    @Override
 *    protected String[] getServletMappings() {//将 DispatcherServlet 映射到 "/" 路径
 * 		return new String[] { "/" };
 *    }
 **/