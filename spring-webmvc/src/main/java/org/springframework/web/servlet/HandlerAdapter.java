
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * HandlerAdapter：称为处理器适配器，主要用于处理MVC的程序调度。在HandlerMapping处理器映射器处理完之后，根据请求的URL找到了相应的早执行的
 * Controller和Controller中相应的方法之后，交给HandlerAdapter，让处理器适配器进行内部调整，返回一个ModelAndView(数据模式和视图对象的关系)
 * 然后HandlerAdapter再交给视图解析器，根据数据模型和视图的关系进行解析，返回视图
 *
 * @see org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
 * @see org.springframework.web.servlet.handler.SimpleServletHandlerAdapter
 */
public interface HandlerAdapter {

	//给定一个处理程序实例，返回这个{@code HandlerAdapter}是否支持它
	boolean supports(Object handler);

	//处理Controller中数据模型和视图关系，返回ModelAndView
	@Nullable
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;


	long getLastModified(HttpServletRequest request, Object handler);

}
