
package org.springframework.web.servlet;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * 用于web交互的MVC视图。实现负责呈现内容，并公开模型。单个视图公开多个模型属性。
 *
 *
 * <p>视图实现可能差异很大。一个明显的实现是基于jsp的。其他实现可能是基于xsl的，或者使用HTML生成库。此接口旨在避免限制可能实现的范围。
 *
 * <p>视图应该是bean。它们很可能被ViewResolver实例化为bean。
 * 由于这个接口是无状态的，视图实现应该是线程安全的。
 *
 * @author Rod Johnson
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @see org.springframework.web.servlet.view.AbstractView
 * @see org.springframework.web.servlet.view.InternalResourceView
 */
public interface View {

	//响应状态
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";

	//路径参数
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";

	//content的类型
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";



	@Nullable
	default String getContentType() {
		return null;
	}

	//渲染视图
	void render(@Nullable Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

}
