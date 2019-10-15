package org.springframework.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * HTTP请求处理程序/控制器的中央分派器，例如web UI控制器或基于HTTP的远程服务导出器。分派到已注册的处理程序处理web请求，提供方便的映射和异常处理设施。
 * <p>
 * 这个servlet非常灵活:它可以与任何工作流一起使用，并安装适当的适配器类。它提供了以下功能，使其有别于其他请求驱动的web MVC框架:
 *
 * <ul>
 * <li>它基于javabean配置机制。
 * 与以下的组件配合工作：
 * (1)、HandlerMapping---处理器映射器，主要使用与URL的映射。
 * (2)、HandlerAdapter---处理器适配器，主要用于处理MVC的程序调度
 * (3)、ViewResolver-----视图解析器，主要用于解析JSP和HTML等视图的
 * (4)、View-------------视图，主要是URL和相应视图的映射
 * (5)、MultipartResolver-二进制解析器，主要作用是解析二进制数据，用于上传文件多。
 * (6)、ContextLoaderListener-监听web容器，一旦启动tomcat，立即加载spring的配置信息，初始化spring的IOC容器
 * (7)、WebApplicationContext--web应用上下文
 * <p>
 * DispatcherServlet:spring mvc的核心控制器，继承FrameworkServlet，而FrameworkServlet继承HttpServletBean并实现ApplicationContextAware
 */
@SuppressWarnings("serial")
public class DispatcherServlet extends FrameworkServlet {


	//与beanFactory中MultipartResolver名称对应，MultipartResolver二进制解析
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

	//与beanFactory中LocaleResolver名称对应，LocaleResolver区域设置解析器
	public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

	//与beanFactory中ThemeResolver名称对应，ThemeResolver主题解析器
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

	//与bean工厂中的HandlerMapping名称对应，HandlerMapping处理器映射器
	public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

	//与bean工厂中HandlerAdapter名称对应,HandlerAdapter视图解析器
	public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

	//与bean工厂中HandlerExceptionResolver,HandlerExceptionResolver处理器异常解析器
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

	//与bean工厂中RequestToViewNameTranslator名称对应，RequestToViewNameTranslator视图名称转换器
	public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";


	//与bean工厂中ViewResolver名称对应，ViewResolver视图解析器，用于解析视图
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";


	//与bean工厂中的FlashMapManager名称对应，FlashMapManager重定向redirect参数的传递管理器
	public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

	/**
	 * 属性来保存当前web应用程序上下文。
	 * 否则，只有全局web应用程序上下文可以通过标签等获得。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#findWebApplicationContext
	 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

	/**
	 * 请求属性来保存当前LocaleResolver，可由视图检索。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocaleResolver
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

	/**
	 * 请求属性来保存当前的ThemeResolver，该属性可由视图检索。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeResolver
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

	/**
	 * 请求属性来保存当前主题资源，该主题资源可由视图检索。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeSource
	 */
	public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

	/**
	 * 保存只读{@code Map}的请求属性的名称
	 * 与“输入”flash属性保存前一个请求，如果有的话。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getInputFlashMap(HttpServletRequest)
	 */
	public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";

	/**
	 * 保存“output”{@link FlashMap}和属性的请求属性的名称，以便为后续请求保存属性。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getOutputFlashMap(HttpServletRequest)
	 */
	public static final String OUTPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";

	/**
	 * 保存{@link FlashMapManager}的请求属性的名称。
	 *
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getFlashMapManager(HttpServletRequest)
	 */
	public static final String FLASH_MAP_MANAGER_ATTRIBUTE = DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";

	//请求属性的名称，该属性公开用
	//但是没有呈现视图(例如设置状态代码)
	public static final String EXCEPTION_ATTRIBUTE = DispatcherServlet.class.getName() + ".EXCEPTION";

	//当没有为请求找到映射处理程序时使用的日志类别
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	//类路径资源的名称(相对于DispatcherServlet类)，它定义DispatcherServlet的默认策略名称。
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";

	//DispatcherServlet的默认策略属性开始的公共前缀。
	private static final String DEFAULT_STRATEGIES_PREFIX = "org.springframework.web.servlet";

	//当URL请求日志
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

	//Properties配置类
	private static final Properties defaultStrategies;

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		/**
		 * 以上翻译：
		 * 从属性文件加载默认策略实现。
		 * 这目前是严格的内部的，并不意味着由应用程序开发人员定制。
		 */
		try {
			//加载spring MVC的配置
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException ex) {
			throw new IllegalStateException("Could not load '" + DEFAULT_STRATEGIES_PATH + "': " + ex.getMessage());
		}
	}

	//检测所有的handlerMapping还是仅仅期望“handlerMapping”bean?
	private boolean detectAllHandlerMappings = true;

	//检测所有的handlerAdapter还是仅仅期望“handlerAdapter”bean?
	private boolean detectAllHandlerAdapters = true;

	//检查所有的HandlerExceptionResolvers，还是仅仅使用这个HandlerExceptionResolvers bean
	private boolean detectAllHandlerExceptionResolvers = true;

	//检查所有的ViewResolvers 的视图解析器，还是只使用这个ViewResolvers
	private boolean detectAllViewResolvers = true;

	//如果没有找到处理此请求的处理程序，则是否抛出NoHandlerFoundException
	private boolean throwExceptionIfNoHandlerFound = false;

	// 在include请求之后是否执行请求属性的清理?
	private boolean cleanupAfterInclude = true;

	//这个servlet使用的MultipartResolver
	@Nullable
	private MultipartResolver multipartResolver;

	//这个servlet使用的LocaleResolver
	@Nullable
	private LocaleResolver localeResolver;

	//这个servlet使用的ThemeResolver
	@Nullable
	private ThemeResolver themeResolver;

	//此servlet使用的HandlerMappings列表--处理器映射器
	@Nullable
	private List<HandlerMapping> handlerMappings;

	//此servlet使用的HandlerAdapter列表---处理器适配器
	@Nullable
	private List<HandlerAdapter> handlerAdapters;

	//此servlet使用的HandlerExceptionResolver列表
	@Nullable
	private List<HandlerExceptionResolver> handlerExceptionResolvers;

	//这个servlet使用的RequestToViewNameTranslator
	@Nullable
	private RequestToViewNameTranslator viewNameTranslator;

	//这个servlet使用的FlashMapManager
	@Nullable
	private FlashMapManager flashMapManager;

	//此servlet使用的ViewResolver列表---视图解析器
	@Nullable
	private List<ViewResolver> viewResolvers;


	/**
	 * 创建一个新的{@code DispatcherServlet}，它将基于servlet提供的默认值和值创建自己的内部web应用程序上下文。而servlet注册的选项是通过{@code web。xml}，要求使用
	 * 无参数构造函数。即在web.xml中配置的org.springframework.web.servlet.DispatcherServlet。
	 * 后续的refresh()会刷新应用程序上下文。
	 *
	 * @see #DispatcherServlet(WebApplicationContext)
	 */
	//Http的URL请求一进来，就会创建实例化DispatcherServlet，和他的父类FrameworkServlet，和父类的父类HttpServletBean，继承的关系一直去实例化。
	//随后就是spring自动去实例这个DispatchServlet类需要的对象出来，比如什么处理器适配器，视图解析器、ApplicationContext等...
	//随后执行该类中的init方法初始化所需的变量对象
	public DispatcherServlet() {
		//先初始化父类FrameworkServlet的构造器，
		super();
		//调用父类的setDispatchOptionsRequest()方法，将所有的HTTP请求分派到doService()方法中
		setDispatchOptionsRequest(true);
	}

	/*
	 * 初始化DispatchServlet时，将Spring web容器的上下文传给Servlet的容器。
	 * 一般传入的是AnnotationConfigWebApplicationContext
	 */
	public DispatcherServlet(WebApplicationContext webApplicationContext) {
		super(webApplicationContext);
		setDispatchOptionsRequest(true);
	}


	//设置检查所有的HandlerMappings
	public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
		this.detectAllHandlerMappings = detectAllHandlerMappings;
	}

	//设置检查所有HandlerAdapters
	public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
		this.detectAllHandlerAdapters = detectAllHandlerAdapters;
	}


	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}


	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
	}

	public void setThrowExceptionIfNoHandlerFound(boolean throwExceptionIfNoHandlerFound) {
		this.throwExceptionIfNoHandlerFound = throwExceptionIfNoHandlerFound;
	}


	public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
		this.cleanupAfterInclude = cleanupAfterInclude;
	}


	/**
	 * This implementation calls {@link #initStrategies}.
	 */
	//刷新web上下文，也是初始化web环境
	@Override
	protected void onRefresh(ApplicationContext context) {
		initStrategies(context);
	}

	/**
	 * 初始化这个servlet使用的策略对象。
	 * 可能在子类中被重写，以便初始化进一步的策略对象。
	 */
	protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		initHandlerExceptionResolvers(context);
		initRequestToViewNameTranslator(context);
		initViewResolvers(context);
		initFlashMapManager(context);
	}

	//初始化MultipartResolver二进制解析器
	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
						"': no multipart request handling provided");
			}
		}
	}

	//初始化LocaleResolver
	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME +
						"': using default [" + this.localeResolver + "]");
			}
		}
	}

	//初始化ThemeResolver
	private void initThemeResolver(ApplicationContext context) {
		try {
			this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using ThemeResolver [" + this.themeResolver + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME +
						"': using default [" + this.themeResolver + "]");
			}
		}
	}

	//初始化处理器映射器
	private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;

		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerMapping> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<>(matchingBeans.values());
				// We keep HandlerMappings in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerMappings);
			}
		} else {
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			} catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		// Ensure we have at least one HandlerMapping, by registering
		// a default HandlerMapping if no other mappings are found.
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	//初始化处理器适配器
	private void initHandlerAdapters(ApplicationContext context) {
		this.handlerAdapters = null;

		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerAdapter> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<>(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerAdapters);
			}
		} else {
			try {
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			} catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	//初始化HandlerExceptionResolvers
	private void initHandlerExceptionResolvers(ApplicationContext context) {
		this.handlerExceptionResolvers = null;

		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<>(matchingBeans.values());
				// We keep HandlerExceptionResolvers in sorted order.
				AnnotationAwareOrderComparator.sort(this.handlerExceptionResolvers);
			}
		} else {
			try {
				HandlerExceptionResolver her =
						context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			} catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
			}
		}

		// Ensure we have at least some HandlerExceptionResolvers, by registering
		// default HandlerExceptionResolvers if no other resolvers are found.
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * 初始化RequestToViewNameTranslator，如果配置没有，则使用默认的DefaultRequestToViewNameTranslator
	 * Initialize the RequestToViewNameTranslator used by this servlet instance.
	 * <p>If no implementation is configured then we default to DefaultRequestToViewNameTranslator.
	 */
	private void initRequestToViewNameTranslator(ApplicationContext context) {
		try {
			this.viewNameTranslator =
					context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
						REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
						"]");
			}
		}
	}

	//初始化InternalResourceViewResolver，如果没有配置，则使用默认的InternalResourceViewResolver
	private void initViewResolvers(ApplicationContext context) {
		this.viewResolvers = null;

		if (this.detectAllViewResolvers) {
			// Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, ViewResolver> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList<>(matchingBeans.values());
				// We keep ViewResolvers in sorted order.
				AnnotationAwareOrderComparator.sort(this.viewResolvers);
			}
		} else {
			try {
				ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
				this.viewResolvers = Collections.singletonList(vr);
			} catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default ViewResolver later.
			}
		}

		// Ensure we have at least one ViewResolver, by registering
		// a default ViewResolver if no other resolvers are found.
		if (this.viewResolvers == null) {
			this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}

	/**
	 * Initialize the {@link FlashMapManager} used by this servlet instance.
	 * <p>If no implementation is configured then we default to
	 * {@code org.springframework.web.servlet.support.DefaultFlashMapManager}.
	 */
	private void initFlashMapManager(ApplicationContext context) {
		try {
			this.flashMapManager = context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using FlashMapManager [" + this.flashMapManager + "]");
			}
		} catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate FlashMapManager with name '" +
						FLASH_MAP_MANAGER_BEAN_NAME + "': using default [" + this.flashMapManager + "]");
			}
		}
	}

	//返回ThemeSource
	@Nullable
	public final ThemeSource getThemeSource() {
		return (getWebApplicationContext() instanceof ThemeSource ? (ThemeSource) getWebApplicationContext() : null);
	}


	@Nullable
	public final MultipartResolver getMultipartResolver() {
		return this.multipartResolver;
	}


	@Nullable
	public final List<HandlerMapping> getHandlerMappings() {
		return (this.handlerMappings != null ? Collections.unmodifiableList(this.handlerMappings) : null);
	}


	protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
		List<T> strategies = getDefaultStrategies(context, strategyInterface);
		if (strategies.size() != 1) {
			throw new BeanInitializationException(
					"DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
		}
		return strategies.get(0);
	}


	@SuppressWarnings("unchecked")
	protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
		String key = strategyInterface.getName();
		String value = defaultStrategies.getProperty(key);
		if (value != null) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
			List<T> strategies = new ArrayList<>(classNames.length);
			for (String className : classNames) {
				try {
					Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
					Object strategy = createDefaultStrategy(context, clazz);
					strategies.add((T) strategy);
				} catch (ClassNotFoundException ex) {
					throw new BeanInitializationException(
							"Could not find DispatcherServlet's default strategy class [" + className +
									"] for interface [" + key + "]", ex);
				} catch (LinkageError err) {
					throw new BeanInitializationException(
							"Unresolvable class definition for DispatcherServlet's default strategy class [" +
									className + "] for interface [" + key + "]", err);
				}
			}
			return strategies;
		} else {
			return new LinkedList<>();
		}
	}


	protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
		return context.getAutowireCapableBeanFactory().createBean(clazz);
	}


	/**
	 * 公开dispatcherservlet特定的请求属性和委托给{@link #doDispatch}用于实际调度。
	 * doService()方法中先把localeResolver、themeResolver以及上下文等放入request的属性中，
	 * 方便后面有需要的人进行使用。根据flashMapManager获取重定向的原有的请求参数。最后调用doDispatch()方法
	 */
	//所有的HTTP请求都会来这里报到，报到完后，会委托给doDispatch()方法去执行具体的请求处理。
	//HttpServletRequest：HTTP请求对象。
	//HttpServletResponse：HTTP相应对象。
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//日志加载
		if (logger.isDebugEnabled()) {
			String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
			logger.debug("DispatcherServlet with name '" + getServletName() + "'" + resumed +
					" processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
		}

		// Keep a snapshot of the request attributes in case of an include,
		// to be able to restore the original attributes after the include.
		//保存请求属性的快照，以防发生include，以便能够在include之后恢复原始属性。
		Map<String, Object> attributesSnapshot = null;
		if (WebUtils.isIncludeRequest(request)) {
			//存储属性的HashMap
			attributesSnapshot = new HashMap<>();
			//取出request对象中的所有属性和属性值
			Enumeration<?> attrNames = request.getAttributeNames();
			//遍历，将其属性存在HashMap中，key为属性名，value为属性值
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
					attributesSnapshot.put(attrName, request.getAttribute(attrName));
				}
			}
		}

		// Make framework objects available to handlers and view objects.
		//使框架对象对处理程序和视图对象可用。初始化以下的bean，环境的依赖
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
		request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());


		if (this.flashMapManager != null) {
			FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
			if (inputFlashMap != null) {
				request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
			}
			request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
			request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);
		}

		try {
			//委托给doDispatch()方法去执行具体的处理流程
			doDispatch(request, response);
		} finally {
			if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
				// Restore the original attribute snapshot, in case of an include.
				//在包含的情况下，还原原始属性快照
				if (attributesSnapshot != null) {
					restoreAttributesAfterInclude(request, attributesSnapshot);
				}
			}
		}
	}

	/**
	 * 处理到处理程序的实际分派。
	 * 处理程序将按顺序应用servlet的HandlerMappings来获得。
	 * HandlerAdapter将通过查询servlet已安装的HandlerAdapter获得，以找到支持处理程序类的第一个HandlerAdapter。
	 * 所有HTTP方法都由这个方法处理。由handleradapter或处理程序本身决定哪些方法是可接受的。
	 *
	 * @param request  当前的HTTP请求对象
	 * @param response 当前HTTP响应对象
	 * @throws Exception in case of any kind of processing failure
	 */
	//实际处理的请求的方法
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//当前线程的请求对象
		HttpServletRequest processedRequest = request;
		//Handler执行链
		HandlerExecutionChain mappedHandler = null;
		//是否二进制请求解析，主要针对文件上传
		boolean multipartRequestParsed = false;
		//获取异步请求管理对象
		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			//视图模型
			ModelAndView mv = null;
			//转发异常
			Exception dispatchException = null;

			try {
				//检查当前线程的Request请求对象，是否为二进制请求(即文件、图片上传的请求)
				processedRequest = checkMultipart(request);
				//是否处理成二进制请求，不是则为false，是则为true
				multipartRequestParsed = (processedRequest != request);
				// Determine handler for the current request.
				//获取HandlerExecutionChain对象Handler执行链，确定当前请求的处理程序。
				//这一步其实就是去找需要执行相应的Controller中和URL对应的方法（即具体的处理程序）的方法
				//获取执行链，即该请求的整体流程
				mappedHandler = getHandler(processedRequest);
				if (mappedHandler == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// Determine handler adapter for the current request.
				//确定当前请求的处理程序适配器。
				//拿相应的Controller对象去获取相应的HandlerAdapter处理器适配器
				//第一种方式实现controller(直接实现 implements Controller )：返回org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter

				//(注解的很重要)
				//第三种注解方式的Controller的适配器：org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
				//做了很多处理，包括去解析注解和参数的适配
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// Process last-modified header, if supported by the handler.
				//获取HTTP方法的请求类型，例如GET、POST或PUT。与CGI变量REQUEST_METHOD的值相同。
				String method = request.getMethod();
				//是否为Get方法
				boolean isGet = "GET".equals(method);
				//如果是Get或Head请求方式,处理last-modified头。
				if (isGet || "HEAD".equals(method)) {
					//使用处理器适配器HandlerAdapter处理last-modified头。
					//org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter中getLastModified
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (logger.isDebugEnabled()) {
						logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
					}
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}

				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				// Actually invoke the handler.
				//获取到了当前Http请求的URL和URL相应的Controller对象，真正要去执行Controller中URL相应的方法
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				if (asyncManager.isConcurrentHandlingStarted()) {
					return;
				}

				//翻译视图名称
				applyDefaultViewName(processedRequest, mv);
				//应用已注册拦截器的事后方法。HandlerExecutionChain执行链中方法
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			} catch (Exception ex) {
				dispatchException = ex;
			} catch (Throwable err) {
				// As of 4.3, we're processing Errors thrown from handler methods as well,
				// making them available for @ExceptionHandler methods and other scenarios.
				dispatchException = new NestedServletException("Handler dispatch failed", err);
			}
			//处理处理程序选择和处理程序调用的结果
			//要么是ModelAndView，要么是要解析为ModelAndView的异常。
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		} catch (Exception ex) {
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		} catch (Throwable err) {
			triggerAfterCompletion(processedRequest, response, mappedHandler,
					new NestedServletException("Handler processing failed", err));
		} finally {
			if (asyncManager.isConcurrentHandlingStarted()) {
				// Instead of postHandle and afterCompletion
				if (mappedHandler != null) {
					//前置处理
					mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
				}
			} else {
				// Clean up any resources used by a multipart request.
				//清理多部分请求使用的任何资源。
				if (multipartRequestParsed) {
					cleanupMultipart(processedRequest);
				}
			}
		}
	}

	//根据request和ModelAndView将需要返回的视图名称设置
	private void applyDefaultViewName(HttpServletRequest request, @Nullable ModelAndView mv) throws Exception {
		if (mv != null && !mv.hasView()) {
			String defaultViewName = getDefaultViewName(request);
			if (defaultViewName != null) {
				mv.setViewName(defaultViewName);
			}
		}
	}

	//处理处理程序选择和处理程序调用的结果
	//要么是ModelAndView，要么是要解析为ModelAndView的异常。
	private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
									   @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
									   @Nullable Exception exception) throws Exception {

		boolean errorView = false;

		if (exception != null) {
			if (exception instanceof ModelAndViewDefiningException) {
				logger.debug("ModelAndViewDefiningException encountered", exception);
				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
			} else {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, exception);
				errorView = (mv != null);
			}
		}

		// Did the handler return a view to render?
		if (mv != null && !mv.wasCleared()) {
			render(mv, request, response);
			if (errorView) {
				WebUtils.clearErrorRequestAttributes(request);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
						"': assuming HandlerAdapter completed request handling");
			}
		}

		if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
			// Concurrent handling started during a forward
			return;
		}

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, null);
		}
	}

	//构建LocaleContext
	@Override
	protected LocaleContext buildLocaleContext(final HttpServletRequest request) {
		LocaleResolver lr = this.localeResolver;
		if (lr instanceof LocaleContextResolver) {
			return ((LocaleContextResolver) lr).resolveLocaleContext(request);
		} else {
			return () -> (lr != null ? lr.resolveLocale(request) : request.getLocale());
		}
	}

	/**
	 * 检查当前线程Http请求，是否为二进制请求(即文件、图片上传的请求)
	 *
	 * @param request 当前线程请求
	 * @return the processed request (multipart wrapper if necessary)
	 * @see MultipartResolver#resolveMultipart
	 */
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		//调用MultipartResolver（二进制解析器）对象检查该request请求是否为二进制处理请求
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			if (WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class) != null) {
				logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, " +
						"this typically results from an additional MultipartFilter in web.xml");
			} else if (hasMultipartException(request)) {
				logger.debug("Multipart resolution previously failed for current request - " +
						"skipping re-resolution for undisturbed error rendering");
			} else {
				try {
					//如果是二进制处理请求，则解析二进制处理请求
					return this.multipartResolver.resolveMultipart(request);
				} catch (MultipartException ex) {
					if (request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE) != null) {
						logger.debug("Multipart resolution failed for error dispatch", ex);
						// Keep processing error dispatch with regular request handle below
					} else {
						throw ex;
					}
				}
			}
		}
		// 如果不是二进制处理请求，则返回原来的请求对象request
		return request;
	}

	/**
	 * Check "javax.servlet.error.exception" attribute for a multipart exception.
	 */
	private boolean hasMultipartException(HttpServletRequest request) {
		Throwable error = (Throwable) request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
		while (error != null) {
			if (error instanceof MultipartException) {
				return true;
			}
			error = error.getCause();
		}
		return false;
	}

	/**
	 * 清理给定多部分请求使用的任何资源(如果有的话)。
	 *
	 * @param request current HTTP request
	 * @see MultipartResolver#cleanupMultipart
	 */
	protected void cleanupMultipart(HttpServletRequest request) {
		if (this.multipartResolver != null) {
			MultipartHttpServletRequest multipartRequest =
					WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
			if (multipartRequest != null) {
				this.multipartResolver.cleanupMultipart(multipartRequest);
			}
		}
	}

	/**
	 * 返回此请求的HandlerExecutionChain。
	 * 按顺序尝试所有处理程序映射。
	 *
	 * @param request current HTTP request
	 * @return the HandlerExecutionChain, or {@code null} if no handler could be found
	 */
	//确定当前HTTP请求的执行链，也就是具体的执行处理流程
	@Nullable
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		//如果处理器映射器集合不为null
		if (this.handlerMappings != null) {
			//遍历出来
			for (HandlerMapping hm : this.handlerMappings) {
				if (logger.isTraceEnabled()) {
					logger.trace(
							"Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
				}
				//去AbstractHandlerMapping对象中获取一条HandlerExecutionChain，Handler执行链
				//HandlerExecutionChain中包含了当前Http请求的信息，URL对象的Controller对象，和两个拦截器
				//直白的说，就是去找当前线程Http请求相应的Controller对象(相应的处理程序，确定程序的执行性)，并在一些处理。
				HandlerExecutionChain handler = hm.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

	/**
	 * 未找到处理程序->设置适当的HTTP响应状态。
	 *
	 * @param request  current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if preparing the response failed
	 */
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + getRequestUri(request) +
					"] in DispatcherServlet with name '" + getServletName() + "'");
		}
		if (this.throwExceptionIfNoHandlerFound) {
			throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
					new ServletServerHttpRequest(request).getHeaders());
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	//根据Controller去获取一个HandlerAdapter
	//Object handler:为当前URL请求需要执行的Controller对象。
	//返回一个org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		if (this.handlerAdapters != null) {
			//遍历handlerAdapters集合
			for (HandlerAdapter ha : this.handlerAdapters) {
				if (logger.isTraceEnabled()) {
					logger.trace("Testing handler adapter [" + ha + "]");
				}
				//获取适合Controller的处理器适配器，
				//这里去执行了org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter中方法
				if (ha.supports(handler)) {
					return ha;
				}
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}

	/**
	 * Determine an error ModelAndView via the registered HandlerExceptionResolvers.
	 *
	 * @param request  current HTTP request
	 * @param response current HTTP response
	 * @param handler  the executed handler, or {@code null} if none chosen at the time of the exception
	 *                 (for example, if multipart resolution failed)
	 * @param ex       the exception that got thrown during handler execution
	 * @return a corresponding ModelAndView to forward to
	 * @throws Exception if no error ModelAndView found
	 */
	@Nullable
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
												   @Nullable Object handler, Exception ex) throws Exception {

		// Check registered HandlerExceptionResolvers...
		ModelAndView exMv = null;
		if (this.handlerExceptionResolvers != null) {
			for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
				exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
				if (exMv != null) {
					break;
				}
			}
		}
		if (exMv != null) {
			if (exMv.isEmpty()) {
				request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
				return null;
			}
			// We might still need view name translation for a plain error model...
			if (!exMv.hasView()) {
				String defaultViewName = getDefaultViewName(request);
				if (defaultViewName != null) {
					exMv.setViewName(defaultViewName);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv, ex);
			}
			WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
			return exMv;
		}

		throw ex;
	}

	/**
	 * 呈现给定的ModelAndView。
	 * 这是处理请求的最后一个阶段。它可能涉及通过名称解析视图。
	 *
	 * @param mv       the ModelAndView to render
	 * @param request  current HTTP servlet request
	 * @param response current HTTP servlet response
	 * @throws ServletException if view is missing or cannot be resolved
	 * @throws Exception        if there's a problem rendering the view
	 */
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Determine locale for request and apply it to the response.
		Locale locale =
				(this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
		response.setLocale(locale);

		View view;
		String viewName = mv.getViewName();
		if (viewName != null) {
			// We need to resolve the view name.
			view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
			if (view == null) {
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
						"' in servlet with name '" + getServletName() + "'");
			}
		} else {
			// No need to lookup: the ModelAndView object contains the actual View object.
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
						"View object in servlet with name '" + getServletName() + "'");
			}
		}

		// Delegate to the View object for rendering.
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
		}
		try {
			if (mv.getStatus() != null) {
				response.setStatus(mv.getStatus().value());
			}
			view.render(mv.getModelInternal(), request, response);
		} catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
						getServletName() + "'", ex);
			}
			throw ex;
		}
	}

	/**
	 * 将提供的请求转换为默认视图名。
	 *
	 * @param request current HTTP servlet request
	 * @return the view name (or {@code null} if no default found)
	 * @throws Exception if view name translation failed
	 */
	@Nullable
	protected String getDefaultViewName(HttpServletRequest request) throws Exception {
		return (this.viewNameTranslator != null ? this.viewNameTranslator.getViewName(request) : null);
	}

	/**
	 * 将给定的视图名称解析为一个视图对象(要呈现)。
	 * 默认的实现会询问这个dispatcher的所有ViewResolvers。
	 * 可以根据特定的模型属性或请求参数重写自定义解析策略。
	 *
	 * @param viewName the name of the view to resolve
	 * @param model    the model to be passed to the view
	 * @param locale   the current locale
	 * @param request  current HTTP servlet request
	 * @return the View object, or {@code null} if none found
	 * @throws Exception if the view cannot be resolved
	 *                   (typically in case of problems creating an actual View object)
	 * @see ViewResolver#resolveViewName
	 */
	@Nullable
	protected View resolveViewName(String viewName, @Nullable Map<String, Object> model,
								   Locale locale, HttpServletRequest request) throws Exception {

		if (this.viewResolvers != null) {
			for (ViewResolver viewResolver : this.viewResolvers) {
				View view = viewResolver.resolveViewName(viewName, locale);
				if (view != null) {
					return view;
				}
			}
		}
		return null;
	}

	private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
										@Nullable HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, ex);
		}
		throw ex;
	}

	/**
	 * 在包含之后恢复请求属性。
	 *
	 * @param request            current HTTP request
	 * @param attributesSnapshot the snapshot of the request attributes before the include
	 */
	@SuppressWarnings("unchecked")
	private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?, ?> attributesSnapshot) {
		// Need to copy into separate Collection here, to avoid side effects
		// on the Enumeration when removing attributes.
		Set<String> attrsToCheck = new HashSet<>();
		Enumeration<?> attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = (String) attrNames.nextElement();
			if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
				attrsToCheck.add(attrName);
			}
		}

		// Add attributes that may have been removed
		attrsToCheck.addAll((Set<String>) attributesSnapshot.keySet());

		// Iterate over the attributes to check, restoring the original value
		// or removing the attribute, respectively, if appropriate.
		for (String attrName : attrsToCheck) {
			Object attrValue = attributesSnapshot.get(attrName);
			if (attrValue == null) {
				request.removeAttribute(attrName);
			} else if (attrValue != request.getAttribute(attrName)) {
				request.setAttribute(attrName, attrValue);
			}
		}
	}

	private static String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return uri;
	}

}
