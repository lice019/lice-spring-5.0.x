
package org.springframework.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.lang.Nullable;
import org.springframework.web.WebApplicationInitializer;

/**
 * 方便的基类用于 {@link WebApplicationInitializer} 实现注册一个spring上文监听器{@link ContextLoaderListener} 到Servlet上下文中
 *
 * <p>子类仅仅需要实现的是{@link #createRootApplicationContext()}, 去执行
 * {@link #registerContextLoaderListener(ServletContext)}.获取一个spring上下文监听器
 *
 * @author Arjen Poutsma
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.2
 */
public abstract class AbstractContextLoaderInitializer implements WebApplicationInitializer {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());


	//启动springmvc的前端控制器
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		registerContextLoaderListener(servletContext);
	}


	//注册ContextLoaderListener监听器
	protected void registerContextLoaderListener(ServletContext servletContext) {
		//创建一个Web应用上下文
		WebApplicationContext rootAppContext = createRootApplicationContext();
		if (rootAppContext != null) {
			//new一个spring的上下文监听器ContextLoaderListener
			ContextLoaderListener listener = new ContextLoaderListener(rootAppContext);
			//注入一个根上下文应用
			listener.setContextInitializers(getRootApplicationContextInitializers());
			//Servlet上下文添加spring上下文的监听器
			servletContext.addListener(listener);
		}
		else {
			logger.debug("No ContextLoaderListener registered, as " +
					"createRootApplicationContext() did not return an application context");
		}
	}


	@Nullable
	protected abstract WebApplicationContext createRootApplicationContext();


	@Nullable
	protected ApplicationContextInitializer<?>[] getRootApplicationContextInitializers() {
		return null;
	}

}
