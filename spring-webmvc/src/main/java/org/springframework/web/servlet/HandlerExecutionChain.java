
package org.springframework.web.servlet;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

/**
 * 处理程序执行链，由处理程序对象和任何处理程序拦截器组成。
 * 由HandlerMapping的{@link HandlerMapping#getHandler}方法返回。
 *
 * HandlerExecutionChain，即处理处理链。它包含了：
 *
 * 一个处理器，即 HandlerMethod
 * 多个拦截器，即 HandlerInterceptor
 *
 * @author Juergen Hoeller
 * @since 20.06.2003
 * @see HandlerInterceptor
 */
public class HandlerExecutionChain {

	private static final Log logger = LogFactory.getLog(HandlerExecutionChain.class);

	//处理器对象
	private final Object handler;

	//多个拦截器，是一个数组
	@Nullable
	private HandlerInterceptor[] interceptors;

	//拦截器集合
	@Nullable
	private List<HandlerInterceptor> interceptorList;

	//拦截器的下标
	private int interceptorIndex = -1;


	/**
	 * 创建一个处理器执行链
	 * @param handler 要执行的处理程序对象
	 */
	public HandlerExecutionChain(Object handler) {
		this(handler, (HandlerInterceptor[]) null);
	}

	/**
	 * 创建一个处理器执行链
	 * @param handler 要执行的处理程序对象
	 * @param interceptors 在处理程序本身执行之前应用的拦截器数组(按照给定的顺序)
	 */
	public HandlerExecutionChain(Object handler, @Nullable HandlerInterceptor... interceptors) {
		//判断Handler是否是HandlerExecutionChain对象，或子类
		if (handler instanceof HandlerExecutionChain) {
			HandlerExecutionChain originalChain = (HandlerExecutionChain) handler;
			//返回要执行的处理程序对象。
			this.handler = originalChain.getHandler();
			this.interceptorList = new ArrayList<>();
			//合并拦截器
			CollectionUtils.mergeArrayIntoCollection(originalChain.getInterceptors(), this.interceptorList);
			CollectionUtils.mergeArrayIntoCollection(interceptors, this.interceptorList);
		}
		else {
			this.handler = handler;
			this.interceptors = interceptors;
		}
	}


	//返回要执行的处理程序对象。
	public Object getHandler() {
		return this.handler;
	}

	//添加拦截器
	public void addInterceptor(HandlerInterceptor interceptor) {
		initInterceptorList().add(interceptor);
	}

	public void addInterceptors(HandlerInterceptor... interceptors) {
		if (!ObjectUtils.isEmpty(interceptors)) {
			CollectionUtils.mergeArrayIntoCollection(interceptors, initInterceptorList());
		}
	}

	//初始化拦截器
	private List<HandlerInterceptor> initInterceptorList() {
		if (this.interceptorList == null) {
			this.interceptorList = new ArrayList<>();
			if (this.interceptors != null) {
				// An interceptor array specified through the constructor
				CollectionUtils.mergeArrayIntoCollection(this.interceptors, this.interceptorList);
			}
		}
		this.interceptors = null;
		return this.interceptorList;
	}


	@Nullable
	public HandlerInterceptor[] getInterceptors() {
		if (this.interceptors == null && this.interceptorList != null) {
			this.interceptors = this.interceptorList.toArray(new HandlerInterceptor[0]);
		}
		return this.interceptors;
	}



	//应用已注册拦截器的预处理方法。
	boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
				if (!interceptor.preHandle(request, response, this.handler)) {
					triggerAfterCompletion(request, response, null);
					return false;
				}
				this.interceptorIndex = i;
			}
		}
		return true;
	}

	//应用注册拦截器后的方法。
	void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
			throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				interceptor.postHandle(request, response, this.handler, mv);
			}
		}
	}

	/**
	 * Trigger afterCompletion callbacks on the mapped HandlerInterceptors.
	 * Will just invoke afterCompletion for all interceptors whose preHandle invocation
	 * has successfully completed and returned true.
	 */
	void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex)
			throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = this.interceptorIndex; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				try {
					interceptor.afterCompletion(request, response, this.handler, ex);
				}
				catch (Throwable ex2) {
					logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
				}
			}
		}
	}


	//在映射的AsyncHandlerInterceptors上应用afterConcurrentHandlerStarted回调
	void applyAfterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response) {
		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				if (interceptors[i] instanceof AsyncHandlerInterceptor) {
					try {
						AsyncHandlerInterceptor asyncInterceptor = (AsyncHandlerInterceptor) interceptors[i];
						asyncInterceptor.afterConcurrentHandlingStarted(request, response, this.handler);
					}
					catch (Throwable ex) {
						logger.error("Interceptor [" + interceptors[i] + "] failed in afterConcurrentHandlingStarted", ex);
					}
				}
			}
		}
	}


	/**
	 * Delegates to the handler's {@code toString()}.
	 */
	@Override
	public String toString() {
		Object handler = getHandler();
		StringBuilder sb = new StringBuilder();
		sb.append("HandlerExecutionChain with handler [").append(handler).append("]");
		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			sb.append(" and ").append(interceptors.length).append(" interceptor");
			if (interceptors.length > 1) {
				sb.append("s");
			}
		}
		return sb.toString();
	}

}
