
package org.springframework.web.servlet;

import java.util.Locale;

import org.springframework.lang.Nullable;

/**
 * ViewResolver:视图解析器，用于解析处理器适配返回的ModelAndView对象，将模型数据解析到指定的视图并渲染
 *
 * 主要作用：将从处理程序返回的基于逻辑字符串的视图名称解析为要呈现给响应的实际视图。
 *
 * 可以按名称解析视图的对象实现的接口。
 * 视图状态在应用程序运行期间不改变，
 * 因此实现可以自由地缓存视图。
 * 实施被鼓励支持国际化，
 * 即本地化视图分辨率。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see org.springframework.web.servlet.view.ResourceBundleViewResolver
 * @see org.springframework.web.servlet.view.XmlViewResolver
 */
public interface ViewResolver {


	//按名称解析给定的视图。返回view视图对象
	@Nullable
	View resolveViewName(String viewName, Locale locale) throws Exception;

}
