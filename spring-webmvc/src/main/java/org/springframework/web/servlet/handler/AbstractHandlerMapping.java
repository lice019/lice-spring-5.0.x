
package org.springframework.web.servlet.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

/**
 * Abstract base class for {@link org.springframework.web.servlet.HandlerMapping}
 * implementations. Supports ordering, a default handler, handler interceptors,
 * including handler interceptors mapped by path patterns.
 *
 * <p>Note: This base class does <i>not</i> support exposure of the
 * {@link #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}. Support for this attribute
 * is up to concrete subclasses, typically based on request URL mappings.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @see #getHandlerInternal
 * @see #setDefaultHandler
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 * @see org.springframework.util.AntPathMatcher
 * @see #setInterceptors
 * @see org.springframework.web.servlet.HandlerInterceptor
 * @since 07.04.2003
 */
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered {

	//默认使用哦处理器
	@Nullable
	private Object defaultHandler;

	//URL路径帮助者
	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	//路径匹配者
	private PathMatcher pathMatcher = new AntPathMatcher();

	//拦截器集合
	private final List<Object> interceptors = new ArrayList<>();

	//处理器拦截器集合
	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<>();

	private final UrlBasedCorsConfigurationSource globalCorsConfigSource = new UrlBasedCorsConfigurationSource();

	private CorsProcessor corsProcessor = new DefaultCorsProcessor();

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered



	public void setDefaultHandler(@Nullable Object defaultHandler) {
		this.defaultHandler = defaultHandler;
	}


	@Nullable
	public Object getDefaultHandler() {
		return this.defaultHandler;
	}


	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
		this.globalCorsConfigSource.setAlwaysUseFullPath(alwaysUseFullPath);
	}


	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
		this.globalCorsConfigSource.setUrlDecode(urlDecode);
	}


	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
		this.globalCorsConfigSource.setRemoveSemicolonContent(removeSemicolonContent);
	}


	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
		this.globalCorsConfigSource.setUrlPathHelper(urlPathHelper);
	}


	public UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}


	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
		this.globalCorsConfigSource.setPathMatcher(pathMatcher);
	}


	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}


	public void setInterceptors(Object... interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}


	public void setCorsConfigurations(Map<String, CorsConfiguration> corsConfigurations) {
		this.globalCorsConfigSource.setCorsConfigurations(corsConfigurations);
	}


	//获取“全局”CORS配置。
	public Map<String, CorsConfiguration> getCorsConfigurations() {
		return this.globalCorsConfigSource.getCorsConfigurations();
	}


	public void setCorsProcessor(CorsProcessor corsProcessor) {
		Assert.notNull(corsProcessor, "CorsProcessor must not be null");
		this.corsProcessor = corsProcessor;
	}


	public CorsProcessor getCorsProcessor() {
		return this.corsProcessor;
	}


	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return this.order;
	}



	//初始化拦截器。
	@Override
	protected void initApplicationContext() throws BeansException {
		extendInterceptors(this.interceptors);
		detectMappedInterceptors(this.adaptedInterceptors);
		initInterceptors();
	}


	protected void extendInterceptors(List<Object> interceptors) {
	}


	protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
		mappedInterceptors.addAll(
				BeanFactoryUtils.beansOfTypeIncludingAncestors(
						obtainApplicationContext(), MappedInterceptor.class, true, false).values());
	}

	/**
	 *初始化指定的拦截器，检查{@link MappedInterceptor}s并在必要时调整{@link HandlerInterceptor}和{@link WebRequestInterceptor}s。
	 *
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				this.adaptedInterceptors.add(adaptInterceptor(interceptor));
			}
		}
	}

	/**
	 * 将给定的拦截器对象调整为{@link HandlerInterceptor}接口。
	 *
	 * @param interceptor the specified interceptor object
	 * @return the interceptor wrapped as HandlerInterceptor
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter
	 */
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		} else if (interceptor instanceof WebRequestInterceptor) {
			return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
		} else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}


	@Nullable
	protected final HandlerInterceptor[] getAdaptedInterceptors() {
		return (!this.adaptedInterceptors.isEmpty() ?
				this.adaptedInterceptors.toArray(new HandlerInterceptor[0]) : null);
	}


	//将所有配置的{@link MappedInterceptor}作为一个数组返回。
	@Nullable
	protected final MappedInterceptor[] getMappedInterceptors() {
		List<MappedInterceptor> mappedInterceptors = new ArrayList<>(this.adaptedInterceptors.size());
		for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
			if (interceptor instanceof MappedInterceptor) {
				mappedInterceptors.add((MappedInterceptor) interceptor);
			}
		}
		return (!mappedInterceptors.isEmpty() ? mappedInterceptors.toArray(new MappedInterceptor[0]) : null);
	}


	/**
	 * 将当前的http请求的request对象传入，
	 *
	 * @param request 当前线程的Http请求
	 * @return
	 * @throws Exception
	 */
	//获取处理器执行链
	@Override
	@Nullable
	public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		//获取一个URL相应的Controller和相应的拦截器。
		//getHandlerInternal(request):这个方法做了很多工作，去获取了当前请求的URL，解析URL的请求参数，并找到当前Http请求的URL的相应Controller对象
		//HandlerExecutionChain with [com.lice.controller.FirstController@1191a5fe] and 1 interceptors
		Object handler = getHandlerInternal(request);
		//如果Handler为null，则获取默认的Handler对象，
		if (handler == null) {
			handler = getDefaultHandler();
		}
		//如果默认的Handler还是null，直接返回null对象
		if (handler == null) {
			return null;
		}
		// Bean name or resolved handler?
		//判断Handler是否为String实例，实际是判断是否是处理过的Handler对象
		if (handler instanceof String) {
			//如果没处理，做以下处理
			String handlerName = (String) handler;
			handler = obtainApplicationContext().getBean(handlerName);
		}
		//返回一个HandlerExecutionChain，里面包含了Http当前请求信息和URL相应Controller和适用的拦截器,
		HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request);
		if (CorsUtils.isCorsRequest(request)) {
			CorsConfiguration globalConfig = this.globalCorsConfigSource.getCorsConfiguration(request);
			CorsConfiguration handlerConfig = getCorsConfiguration(handler, request);
			CorsConfiguration config = (globalConfig != null ? globalConfig.combine(handlerConfig) : handlerConfig);
			executionChain = getCorsHandlerExecutionChain(request, executionChain, config);
		}
		//返回一个HandlerExecutionChain，里面包含了Http当前请求信息和URL相应Controller和适用的拦截器,
		return executionChain;
	}


	@Nullable
	protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;


	//获取HandlerExecutionChain，里面包含了Http当前请求信息和URL相应Controller，
	//为给定的处理程序构建{@link HandlerExecutionChain}，包括适用的拦截器。
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
				(HandlerExecutionChain) handler : new HandlerExecutionChain(handler));

		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
			if (interceptor instanceof MappedInterceptor) {
				MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
				if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
					chain.addInterceptor(mappedInterceptor.getInterceptor());
				}
			} else {
				//将ConversionServiceExposingInterceptor拦截器添加到HandlerExecutionChain执行链中
				chain.addInterceptor(interceptor);
			}
		}
		//返回含有ConversionServiceExposingInterceptor的执行链
		return chain;
	}


	@Nullable
	protected CorsConfiguration getCorsConfiguration(Object handler, HttpServletRequest request) {
		Object resolvedHandler = handler;
		if (handler instanceof HandlerExecutionChain) {
			resolvedHandler = ((HandlerExecutionChain) handler).getHandler();
		}
		if (resolvedHandler instanceof CorsConfigurationSource) {
			return ((CorsConfigurationSource) resolvedHandler).getCorsConfiguration(request);
		}
		return null;
	}


	protected HandlerExecutionChain getCorsHandlerExecutionChain(HttpServletRequest request,
																 HandlerExecutionChain chain, @Nullable CorsConfiguration config) {

		if (CorsUtils.isPreFlightRequest(request)) {
			HandlerInterceptor[] interceptors = chain.getInterceptors();
			chain = new HandlerExecutionChain(new PreFlightHandler(config), interceptors);
		} else {
			chain.addInterceptor(new CorsInterceptor(config));
		}
		return chain;
	}


	private class PreFlightHandler implements HttpRequestHandler, CorsConfigurationSource {

		@Nullable
		private final CorsConfiguration config;

		public PreFlightHandler(@Nullable CorsConfiguration config) {
			this.config = config;
		}

		@Override
		public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
			corsProcessor.processRequest(this.config, request, response);
		}

		@Override
		@Nullable
		public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
			return this.config;
		}
	}


	private class CorsInterceptor extends HandlerInterceptorAdapter implements CorsConfigurationSource {

		@Nullable
		private final CorsConfiguration config;

		public CorsInterceptor(@Nullable CorsConfiguration config) {
			this.config = config;
		}

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {

			return corsProcessor.processRequest(this.config, request, response);
		}

		@Override
		@Nullable
		public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
			return this.config;
		}
	}

}
