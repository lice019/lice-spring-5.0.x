
package org.springframework.web.servlet.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * HttpRequestHandlerAdapter：Http请求的处理器适配器
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.HttpRequestHandler
 * @see LastModified
 * @see SimpleControllerHandlerAdapter
 */
public class HttpRequestHandlerAdapter implements HandlerAdapter {

	//判断Handler是否为HttpRequestHandler实例或子类，实现类
	//即判断是否为Controller
	@Override
	public boolean supports(Object handler) {
		//实现Controller的方式之一，实现HttpRequestHandler接口，然后实现接口中的handleRequest
		return (handler instanceof HttpRequestHandler);
	}

	//返回ModelAndView
	@Override
	@Nullable
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		((HttpRequestHandler) handler).handleRequest(request, response);
		return null;
	}

	@Override
	public long getLastModified(HttpServletRequest request, Object handler) {
		if (handler instanceof LastModified) {
			return ((LastModified) handler).getLastModified(request);
		}
		return -1L;
	}

}
