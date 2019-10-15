
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * 解决异常的策略，可能将异常映射到处理程序、HTML错误视图或其他目标。看到异常。
 */
public interface HandlerExceptionResolver {


	@Nullable
	ModelAndView resolveException(
			HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex);

}
