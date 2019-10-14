
package org.springframework.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * 在Servlet3.0规范中，就可以使用java方式配置取代了web.xml
 * 接口将在Servlet3.0+环境中实现，以便以编程方式配置{@link servletContext}——与传统的基于{@code web.xml}的方法相反（或可能与之结合）。
 *
 * <h2>c传统的web配置方式：</h2>
 * <h3>The traditional, XML-based approach</h3>
 * <pre class="code">
 * {@code
 * <servlet>
 *   <servlet-name>dispatcher</servlet-name>
 *   <servlet-class>
 *     org.springframework.web.servlet.DispatcherServlet
 *   </servlet-class>
 *   <init-param>
 *     <param-name>contextConfigLocation</param-name>
 *     <param-value>/WEB-INF/spring/dispatcher-config.xml</param-value>
 *   </init-param>
 *   <load-on-startup>1</load-on-startup>
 * </servlet>
 *
 * <servlet-mapping>
 *   <servlet-name>dispatcher</servlet-name>
 *   <url-pattern>/</url-pattern>
 * </servlet-mapping>}</pre>
 *
 * 基于WebApplicationInitializer方式：
 * 基于{代码WebApplicationInitializer } 的基于代码的方法
 * 下面是等效的{注册码，{代码WebApplicationInitializer }样式：
 *
 *
 * public class MyWebAppInitializer implements WebApplicationInitializer {
 *
 *    &#064;Override
 *    public void onStartup(ServletContext container) {
 *      XmlWebApplicationContext appContext = new XmlWebApplicationContext();
 *      appContext.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
 *
 *      ServletRegistration.Dynamic dispatcher =
 *        container.addServlet("dispatcher", new DispatcherServlet(appContext));
 *      dispatcher.setLoadOnStartup(1);
 *      dispatcher.addMapping("/");
 *    }
 *
 * }</pre>
 *
 * As an alternative to the above, you can also extend from {@link
 * org.springframework.web.servlet.support.AbstractDispatcherServletInitializer}.
 *
 * As you can see, thanks to Servlet 3.0's new {@link ServletContext#addServlet} method
 * we're actually registering an <em>instance</em> of the {@code DispatcherServlet}, and
 * this means that the {@code DispatcherServlet} can now be treated like any other object
 * -- receiving constructor injection of its application context in this case.
 *
 * <p>This style is both simpler and more concise. There is no concern for dealing with
 * init-params, etc, just normal JavaBean-style properties and constructor arguments. You
 * are free to create and work with your Spring application contexts as necessary before
 * injecting them into the {@code DispatcherServlet}.
 *
 * <p>Most major Spring Web components have been updated to support this style of
 * registration.  You'll find that {@code DispatcherServlet}, {@code FrameworkServlet},
 * {@code ContextLoaderListener} and {@code DelegatingFilterProxy} all now support
 * constructor arguments. Even if a component (e.g. non-Spring, other third party) has not
 * been specifically updated for use within {@code WebApplicationInitializers}, they still
 * may be used in any case. The Servlet 3.0 {@code ServletContext} API allows for setting
 * init-params, context-params, etc programmatically.
 *
 * <h2>A 100% code-based approach to configuration</h2>
 * In the example above, {@code WEB-INF/web.xml} was successfully replaced with code in
 * the form of a {@code WebApplicationInitializer}, but the actual
 * {@code dispatcher-config.xml} Spring configuration remained XML-based.
 * {@code WebApplicationInitializer} is a perfect fit for use with Spring's code-based
 * {@code @Configuration} classes. See @{@link
 * org.springframework.context.annotation.Configuration Configuration} Javadoc for
 * complete details, but the following example demonstrates refactoring to use Spring's
 * {@link org.springframework.web.context.support.AnnotationConfigWebApplicationContext
 * AnnotationConfigWebApplicationContext} in lieu of {@code XmlWebApplicationContext}, and
 * user-defined {@code @Configuration} classes {@code AppConfig} and
 * {@code DispatcherConfig} instead of Spring XML files. This example also goes a bit
 * beyond those above to demonstrate typical configuration of the 'root' application
 * context and registration of the {@code ContextLoaderListener}:
 * <pre class="code">
 *     -----javabean的配置方式
 * public class MyWebAppInitializer implements WebApplicationInitializer {
 *
 *    &#064;Override
 *    public void onStartup(ServletContext container) {
 *      // Create the 'root' Spring application context
 *      AnnotationConfigWebApplicationContext rootContext =
 *        new AnnotationConfigWebApplicationContext();
 *      rootContext.register(AppConfig.class);
 *
 *      // Manage the lifecycle of the root application context
 *      container.addListener(new ContextLoaderListener(rootContext));
 *
 *      // Create the dispatcher servlet's Spring application context
 *      AnnotationConfigWebApplicationContext dispatcherContext =
 *        new AnnotationConfigWebApplicationContext();
 *      dispatcherContext.register(DispatcherConfig.class);
 *
 *      // Register and map the dispatcher servlet
 *      ServletRegistration.Dynamic dispatcher =
 *        container.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
 *      dispatcher.setLoadOnStartup(1);
 *      dispatcher.addMapping("/");
 *    }
 *
 * }</pre>
 *
 * As an alternative to the above, you can also extend from {@link
 * org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer}.
 *
 * Remember that {@code WebApplicationInitializer} implementations are <em>detected
 * automatically</em> -- so you are free to package them within your application as you
 * see fit.
 *
 * <h2>Ordering {@code WebApplicationInitializer} execution</h2>
 * {@code WebApplicationInitializer} implementations may optionally be annotated at the
 * class level with Spring's @{@link org.springframework.core.annotation.Order Order}
 * annotation or may implement Spring's {@link org.springframework.core.Ordered Ordered}
 * interface. If so, the initializers will be ordered prior to invocation. This provides
 * a mechanism for users to ensure the order in which servlet container initialization
 * occurs. Use of this feature is expected to be rare, as typical applications will likely
 * centralize all container initialization within a single {@code WebApplicationInitializer}.
 *
 * <h2>Caveats</h2>
 *
 * <h3>web.xml versioning</h3>
 * <p>{@code WEB-INF/web.xml} and {@code WebApplicationInitializer} use are not mutually
 * exclusive; for example, web.xml can register one servlet, and a {@code
 * WebApplicationInitializer} can register another. An initializer can even
 * <em>modify</em> registrations performed in {@code web.xml} through methods such as
 * {@link ServletContext#getServletRegistration(String)}. <strong>However, if
 * {@code WEB-INF/web.xml} is present in the application, its {@code version} attribute
 * must be set to "3.0" or greater, otherwise {@code ServletContainerInitializer}
 * bootstrapping will be ignored by the servlet container.</strong>
 *
 * <h3>Mapping to '/' under Tomcat</h3>
 * <p>Apache Tomcat maps its internal {@code DefaultServlet} to "/", and on Tomcat versions
 * &lt;= 7.0.14, this servlet mapping <em>cannot be overridden programmatically</em>.
 * 7.0.15 fixes this issue. Overriding the "/" servlet mapping has also been tested
 * successfully under GlassFish 3.1.<p>
 *
 * @author Chris Beams
 * @since 3.1
 * @see SpringServletContainerInitializer
 * @see org.springframework.web.context.AbstractContextLoaderInitializer
 * @see org.springframework.web.servlet.support.AbstractDispatcherServletInitializer
 * @see org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer
 */
public interface WebApplicationInitializer {

	/**
	 * Configure the given {@link ServletContext} with any servlets, filters, listeners
	 * context-params and attributes necessary for initializing this web application. See
	 * examples {@linkplain WebApplicationInitializer above}.
	 * @param servletContext the {@code ServletContext} to initialize
	 * @throws ServletException if any call against the given {@code ServletContext}
	 * throws a {@code ServletException}
	 */
	void onStartup(ServletContext servletContext) throws ServletException;

}
