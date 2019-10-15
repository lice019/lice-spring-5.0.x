package org.springframework.web;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

/**
 *SpringServletContainerInitializer：实现了Servlet3.0规范的ServletContainerInitializer接口，
 * 作用在于tomcat启动时，初始化Servlet容器和初始化Spring的容器
 *
 * @see #onStartup(Set, ServletContext)
 * @see WebApplicationInitializer
 * @since 3.1
 */
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {


	/*
	 * 基于Servlet3.0后的规范
	 * spring使用SPI技术通过初始Servlet的容器，SPI将resources中javax.servlet.ServletContainerInitializer配置将ServletContainerInitializer接口的所有实现类class字节码
	 * 全部进行加载到onStartup方法参数中Set<Class<?>> webAppInitializerClasses。
	 *
	 *ServletContainerInitializer：是tomcat中的接口，Servlet容器初始化类
	 */
	@Override
	public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		//创建一个List集合存储ServletContainerInitializer所有实现类的Class字节码
		List<WebApplicationInitializer> initializers = new LinkedList<>();

		if (webAppInitializerClasses != null) {

			//遍历所有ServletContainerInitializer实现类的字节码
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						//将ServletContainerInitializer所有实现类通过反射创建实例，存储到List集合中
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					} catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		//将ServletContainerInitializer所有实现类实例对象进行排序
		//排序的目的是为了bean实例的执行顺序（一般排序都是spring内部定义的bean对象，用于初始化环境所需要用到的类对象）
		//因为是容器环境的初始化，所有需要按一定的顺序步骤来处理
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

}
