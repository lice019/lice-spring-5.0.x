package org.springframework.web.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * FrameworkServlet是springMVC初始化IOC容器的核心，通过读取配置的contextConfigLocation配置的springMVC配置文件的地址来读取各种初始化信息和Bean注入信息，
 * 来完成springMVC IOC 容器的初始化。
 */
@SuppressWarnings("serial")
public abstract class FrameworkServlet extends HttpServletBean implements ApplicationContextAware {


	//WebApplicationContext的名称前缀
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";


	//FrameworkServlet默认使用的上下文类，是一个基于XML配置的WebApplicationContext
	public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;

	//WebApplicationContext的ServletContext属性的前缀。完成是servlet名称。
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";

	//这些字符中的任意数量都被认为是在一个init-param字符串值中包含多个值的分隔符。
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";


	//属性查找其中的WebApplicationContext
	@Nullable
	private String contextAttribute;

	//要创建的WebApplicationContext实现类
	private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;

	//给WebApplicationContext分配id
	@Nullable
	private String contextId;

	//Servlet的命名空间
	@Nullable
	private String namespace;

	//显式上下文配置位置
	@Nullable
	private String contextConfigLocation;


	//要应用于上下文的实际ApplicationContextInitializer实例
	private final List<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers =
			new ArrayList<>();


	@Nullable
	private String contextInitializerClasses;


	//是否为上下文作为ServletContext 发布
	private boolean publishContext = true;


	private boolean publishEvents = true;


	private boolean threadContextInheritable = false;


	private boolean dispatchOptionsRequest = false;


	private boolean dispatchTraceRequest = false;


	//这个servlet的WebApplicationContext
	@Nullable
	private WebApplicationContext webApplicationContext;


	private boolean webApplicationContextInjected = false;


	private volatile boolean refreshEventReceived = false;


	//用于同步onRefresh执行的监视器
	private final Object onRefreshMonitor = new Object();


	//初始化FrameworkServlet前，会先初始化父类HttpServletBean
	public FrameworkServlet() {
	}

	//创建一个FrameworkServlet时，初始化web应用的上下文环境
	public FrameworkServlet(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
	}



	public void setContextAttribute(@Nullable String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}


	@Nullable
	public String getContextAttribute() {
		return this.contextAttribute;
	}


	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Return the custom context class.
	 */
	public Class<?> getContextClass() {
		return this.contextClass;
	}


	public void setContextId(@Nullable String contextId) {
		this.contextId = contextId;
	}


	@Nullable
	public String getContextId() {
		return this.contextId;
	}


	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}


	public String getNamespace() {
		return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
	}


	public void setContextConfigLocation(@Nullable String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}


	@Nullable
	public String getContextConfigLocation() {
		return this.contextConfigLocation;
	}


	@SuppressWarnings("unchecked")
	public void setContextInitializers(@Nullable ApplicationContextInitializer<?>... initializers) {
		if (initializers != null) {
			for (ApplicationContextInitializer<?> initializer : initializers) {
				this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
			}
		}
	}


	public void setContextInitializerClasses(String contextInitializerClasses) {
		this.contextInitializerClasses = contextInitializerClasses;
	}


	public void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}


	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}


	public void setThreadContextInheritable(boolean threadContextInheritable) {
		this.threadContextInheritable = threadContextInheritable;
	}


	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}

	//设置此servlet是否应该将HTTP跟踪请求发送到{@link #doService}方法。
	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		//设置WebApplicationContext
		if (this.webApplicationContext == null && applicationContext instanceof WebApplicationContext) {
			this.webApplicationContext = (WebApplicationContext) applicationContext;
			this.webApplicationContextInjected = true;
		}
	}


	/**
	 * 重写{@link HttpServletBean}的方法，在设置任何bean属性之后调用。创建此servlet的WebApplicationContext。
	 */
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
		if (logger.isInfoEnabled()) {
			logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
		}
		long startTime = System.currentTimeMillis();

		try {
			this.webApplicationContext = initWebApplicationContext();
			initFrameworkServlet();
		} catch (ServletException | RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
					elapsedTime + " ms");
		}
	}

	protected WebApplicationContext initWebApplicationContext() {
		WebApplicationContext rootContext =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		if (this.webApplicationContext != null) {
			// A context instance was injected at construction time -> use it
			wac = this.webApplicationContext;
			if (wac instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
				if (!cwac.isActive()) {
					// The context has not yet been refreshed -> provide services such as
					// setting the parent context, setting the application context id, etc
					if (cwac.getParent() == null) {
						// The context instance was injected without an explicit parent -> set
						// the root application context (if any; may be null) as the parent
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);
				}
			}
		}
		if (wac == null) {
			// No context instance was injected at construction time -> see if one
			// has been registered in the servlet context. If one exists, it is assumed
			// that the parent context (if any) has already been set and that the
			// user has performed any initialization such as setting the context id
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// No context instance is defined for this servlet -> create a local one
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// Either the context is not a ConfigurableApplicationContext with refresh
			// support or the context injected at construction time had already been
			// refreshed -> trigger initial onRefresh manually here.
			synchronized (this.onRefreshMonitor) {
				onRefresh(wac);
			}
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute.
			String attrName = getServletContextAttributeName();
			getServletContext().setAttribute(attrName, wac);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
						"' as ServletContext attribute with name [" + attrName + "]");
			}
		}

		return wac;
	}


	@Nullable
	protected WebApplicationContext findWebApplicationContext() {
		String attrName = getContextAttribute();
		if (attrName == null) {
			return null;
		}
		WebApplicationContext wac =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
		}
		return wac;
	}

	protected WebApplicationContext createWebApplicationContext(@Nullable ApplicationContext parent) {
		Class<?> contextClass = getContextClass();
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Servlet with name '" + getServletName() +
					"' will try to create custom WebApplicationContext context of class '" +
					contextClass.getName() + "'" + ", using parent context [" + parent + "]");
		}
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException(
					"Fatal initialization error in servlet with name '" + getServletName() +
							"': custom WebApplicationContext class [" + contextClass.getName() +
							"] is not of type ConfigurableWebApplicationContext");
		}
		ConfigurableWebApplicationContext wac =
				(ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

		wac.setEnvironment(getEnvironment());
		wac.setParent(parent);
		String configLocation = getContextConfigLocation();
		if (configLocation != null) {
			wac.setConfigLocation(configLocation);
		}
		configureAndRefreshWebApplicationContext(wac);

		return wac;
	}

	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value
			// -> assign a more useful id based on available information
			if (this.contextId != null) {
				wac.setId(this.contextId);
			} else {
				// Generate default id...
				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
						ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
			}
		}

		wac.setServletContext(getServletContext());
		wac.setServletConfig(getServletConfig());
		wac.setNamespace(getNamespace());
		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

		// The wac environment's #initPropertySources will be called in any case when the context
		// is refreshed; do it eagerly here to ensure servlet property sources are in place for
		// use in any post-processing or initialization that occurs below prior to #refresh
		ConfigurableEnvironment env = wac.getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
		}

		postProcessWebApplicationContext(wac);
		applyInitializers(wac);
		wac.refresh();
	}


	protected WebApplicationContext createWebApplicationContext(@Nullable WebApplicationContext parent) {
		return createWebApplicationContext((ApplicationContext) parent);
	}


	protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {
	}


	protected void applyInitializers(ConfigurableApplicationContext wac) {
		String globalClassNames = getServletContext().getInitParameter(ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM);
		if (globalClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		if (this.contextInitializerClasses != null) {
			for (String className : StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		AnnotationAwareOrderComparator.sort(this.contextInitializers);
		for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
			initializer.initialize(wac);
		}
	}

	@SuppressWarnings("unchecked")
	private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(
			String className, ConfigurableApplicationContext wac) {
		try {
			Class<?> initializerClass = ClassUtils.forName(className, wac.getClassLoader());
			Class<?> initializerContextClass =
					GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
			if (initializerContextClass != null && !initializerContextClass.isInstance(wac)) {
				throw new ApplicationContextException(String.format(
						"Could not apply context initializer [%s] since its generic parameter [%s] " +
								"is not assignable from the type of application context used by this " +
								"framework servlet: [%s]", initializerClass.getName(), initializerContextClass.getName(),
						wac.getClass().getName()));
			}
			return BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
		} catch (ClassNotFoundException ex) {
			throw new ApplicationContextException(String.format("Could not load class [%s] specified " +
					"via 'contextInitializerClasses' init-param", className), ex);
		}
	}


	public String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}


	@Nullable
	public final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}



	protected void initFrameworkServlet() throws ServletException {
	}

	//刷新这个servlet的应用程序上下文，以及servlet的依赖状态。
	public void refresh() {
		WebApplicationContext wac = getWebApplicationContext();
		if (!(wac instanceof ConfigurableApplicationContext)) {
			throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
		}
		((ConfigurableApplicationContext) wac).refresh();
	}


	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.refreshEventReceived = true;
		synchronized (this.onRefreshMonitor) {
			onRefresh(event.getApplicationContext());
		}
	}


	protected void onRefresh(ApplicationContext context) {
		// For subclasses: do nothing by default.
	}


	@Override
	public void destroy() {
		getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
		// Only call close() on WebApplicationContext if locally managed...
		if (this.webApplicationContext instanceof ConfigurableApplicationContext && !this.webApplicationContextInjected) {
			((ConfigurableApplicationContext) this.webApplicationContext).close();
		}
	}


	//重写父类实现，以拦截补丁请求。
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
		if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
			processRequest(request, response);
		} else {
			super.service(request, response);
		}
	}


	/**
	 * @see #doHead
	 */
	//委托Get方式的请求给processRequest/doService.
	//<P>也将由HttpServlet默认实现的{@代码doHead }调用，其中{@代码NoBodyResponse }仅捕获内容长度。
	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate POST requests to {@link #processRequest}.
	 *
	 * @see #doService
	 */
	//委托POST请求给processRequest
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate PUT requests to {@link #processRequest}.
	 *
	 * @see #doService
	 */
	//委托PUT请求给processRequest
	@Override
	protected final void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate DELETE requests to {@link #processRequest}.
	 *
	 * @see #doService
	 */
	//委托delete请求给processRequest
	@Override
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		processRequest(request, response);
	}

	/**
	 * Delegate OPTIONS requests to {@link #processRequest}, if desired.
	 * <p>Applies HttpServlet's standard OPTIONS processing otherwise,
	 * and also if there is still no 'Allow' header set after dispatching.
	 *
	 * @see #doService
	 */
	//委托options请求给processRequest
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchOptionsRequest || CorsUtils.isPreFlightRequest(request)) {
			processRequest(request, response);
			if (response.containsHeader("Allow")) {
				// Proper OPTIONS response coming from a handler - we're done.
				return;
			}
		}

		// Use response wrapper in order to always add PATCH to the allowed methods
		super.doOptions(request, new HttpServletResponseWrapper(response) {
			@Override
			public void setHeader(String name, String value) {
				if ("Allow".equals(name)) {
					value = (StringUtils.hasLength(value) ? value + ", " : "") + HttpMethod.PATCH.name();
				}
				super.setHeader(name, value);
			}
		});
	}

	/**
	 * Delegate TRACE requests to {@link #processRequest}, if desired.
	 * <p>Applies HttpServlet's standard TRACE processing otherwise.
	 *
	 * @see #doService
	 */
	//委托trace请求给processRequest
	@Override
	protected void doTrace(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (this.dispatchTraceRequest) {
			processRequest(request, response);
			if ("message/http".equals(response.getContentType())) {
				// Proper TRACE response coming from a handler - we're done.
				return;
			}
		}
		//调用HTTPServlet的doTrace方法
		super.doTrace(request, response);
	}

	/**
	 * Process this request, publishing an event regardless of the outcome.
	 * <p>The actual event handling is performed by the abstract
	 * {@link #doService} template method.
	 */
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContext localeContext = buildLocaleContext(request);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

		initContextHolders(request, localeContext, requestAttributes);

		try {
			doService(request, response);
		} catch (ServletException | IOException ex) {
			failureCause = ex;
			throw ex;
		} catch (Throwable ex) {
			failureCause = ex;
			throw new NestedServletException("Request processing failed", ex);
		} finally {
			resetContextHolders(request, previousLocaleContext, previousAttributes);
			if (requestAttributes != null) {
				requestAttributes.requestCompleted();
			}

			if (logger.isDebugEnabled()) {
				if (failureCause != null) {
					this.logger.debug("Could not complete request", failureCause);
				} else {
					if (asyncManager.isConcurrentHandlingStarted()) {
						logger.debug("Leaving response open for concurrent processing");
					} else {
						this.logger.debug("Successfully completed request");
					}
				}
			}

			publishRequestHandledEvent(request, response, startTime, failureCause);
		}
	}

	/**
	 * Build a LocaleContext for the given request, exposing the request's
	 * primary locale as current locale.
	 *
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext, or {@code null} if none to bind
	 * @see LocaleContextHolder#setLocaleContext
	 */
	@Nullable
	protected LocaleContext buildLocaleContext(HttpServletRequest request) {
		return new SimpleLocaleContext(request.getLocale());
	}

	/**
	 * Build ServletRequestAttributes for the given request (potentially also
	 * holding a reference to the response), taking pre-bound attributes
	 * (and their type) into consideration.
	 *
	 * @param request            current HTTP request
	 * @param response           current HTTP response
	 * @param previousAttributes pre-bound RequestAttributes instance, if any
	 * @return the ServletRequestAttributes to bind, or {@code null} to preserve
	 * the previously bound instance (or not binding any, if none bound before)
	 * @see RequestContextHolder#setRequestAttributes
	 */
	@Nullable
	protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request,
															  @Nullable HttpServletResponse response, @Nullable RequestAttributes previousAttributes) {

		if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
			return new ServletRequestAttributes(request, response);
		} else {
			return null;  // preserve the pre-bound RequestAttributes instance
		}
	}

	private void initContextHolders(HttpServletRequest request,
									@Nullable LocaleContext localeContext, @Nullable RequestAttributes requestAttributes) {

		if (localeContext != null) {
			LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
		}
		if (requestAttributes != null) {
			RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Bound request context to thread: " + request);
		}
	}

	private void resetContextHolders(HttpServletRequest request,
									 @Nullable LocaleContext prevLocaleContext, @Nullable RequestAttributes previousAttributes) {

		LocaleContextHolder.setLocaleContext(prevLocaleContext, this.threadContextInheritable);
		RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
		if (logger.isTraceEnabled()) {
			logger.trace("Cleared thread-bound request context: " + request);
		}
	}

	private void publishRequestHandledEvent(HttpServletRequest request, HttpServletResponse response,
											long startTime, @Nullable Throwable failureCause) {

		if (this.publishEvents && this.webApplicationContext != null) {
			// Whether or not we succeeded, publish an event.
			long processingTime = System.currentTimeMillis() - startTime;
			this.webApplicationContext.publishEvent(
					new ServletRequestHandledEvent(this,
							request.getRequestURI(), request.getRemoteAddr(),
							request.getMethod(), getServletConfig().getServletName(),
							WebUtils.getSessionId(request), getUsernameForRequest(request),
							processingTime, failureCause, response.getStatus()));
		}
	}

	/**
	 * Determine the username for the given request.
	 * <p>The default implementation takes the name of the UserPrincipal, if any.
	 * Can be overridden in subclasses.
	 *
	 * @param request current HTTP request
	 * @return the username, or {@code null} if none found
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	@Nullable
	protected String getUsernameForRequest(HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		return (userPrincipal != null ? userPrincipal.getName() : null);
	}


	/**
	 * Subclasses must implement this method to do the work of request handling,
	 * receiving a centralized callback for GET, POST, PUT and DELETE.
	 * <p>The contract is essentially the same as that for the commonly overridden
	 * {@code doGet} or {@code doPost} methods of HttpServlet.
	 * <p>This class intercepts calls to ensure that exception handling and
	 * event publication takes place.
	 *
	 * @param request  current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of any kind of processing failure
	 * @see javax.servlet.http.HttpServlet#doGet
	 * @see javax.servlet.http.HttpServlet#doPost
	 */
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response)
			throws Exception;


	/**
	 * ApplicationListener endpoint that receives events from this servlet's WebApplicationContext
	 * only, delegating to {@code onApplicationEvent} on the FrameworkServlet instance.
	 */
	private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			FrameworkServlet.this.onApplicationEvent(event);
		}
	}


	/**
	 * CallableProcessingInterceptor implementation that initializes and resets
	 * FrameworkServlet's context holders, i.e. LocaleContextHolder and RequestContextHolder.
	 */
	private class RequestBindingInterceptor implements CallableProcessingInterceptor {

		@Override
		public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
				initContextHolders(request, buildLocaleContext(request),
						buildRequestAttributes(request, response, null));
			}
		}

		@Override
		public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, Object concurrentResult) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				resetContextHolders(request, null, null);
			}
		}
	}

}
