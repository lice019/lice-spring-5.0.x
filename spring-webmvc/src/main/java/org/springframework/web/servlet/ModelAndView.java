
package org.springframework.web.servlet;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;

/**
 * 在web MVC框架中模型和视图的Holder。
 * 注意，它们是完全不同的。这个类只是同时持有模型和视图，使控制器能够在一个返回值中同时返回模型和视图
 *
 * <p>表示处理程序返回的要解析的模型和视图
 * DispatcherServlet。视图可以采用字符串视图名称的形式，需要通过ViewResolver对象解析;或者，可以直接指定视图对象。该模型是一个映射，允许使用多个按名称键控的对象。
 *
 * @see DispatcherServlet
 * @see ViewResolver
 * @see HandlerAdapter#handle
 * @see org.springframework.web.servlet.mvc.Controller#handleRequest
 */
public class ModelAndView {


	//视图实例或视图名称字符串
	@Nullable
	private Object view;


	//模型图--即模型的映射
	@Nullable
	private ModelMap model;


	//响应的可选HTTP状态
	@Nullable
	private HttpStatus status;


	//指示是否通过调用{@link #clear()}清除此实例
	private boolean cleared = false;


	/**
	 * bean样式使用的默认构造函数:填充bean属性而不是传递构造函数参数。
	 *
	 * @see #setView(View)--视图对象
	 * @see #setViewName(String)---视图名称
	 */
	public ModelAndView() {
	}

	//根据视图名称，创建ModelAndView实例
	public ModelAndView(String viewName) {
		this.view = viewName;
	}


	public ModelAndView(View view) {
		this.view = view;
	}

	/**
	 * Create a new ModelAndView given a view name and a model.
	 *
	 * @param viewName name of the View to render, to be resolved
	 *                 by the DispatcherServlet's ViewResolver
	 * @param model    a Map of model names (Strings) to model objects
	 *                 (Objects). Model entries may not be {@code null}, but the
	 *                 model Map may be {@code null} if there is no model data.
	 */
	//给定视图名称和模型，创建一个新的ModelAndView。S
	public ModelAndView(String viewName, @Nullable Map<String, ?> model) {
		this.view = viewName;
		if (model != null) {
			//设置模型名称
			getModelMap().addAllAttributes(model);
		}
	}


	public ModelAndView(View view, @Nullable Map<String, ?> model) {
		this.view = view;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
	}


	public ModelAndView(String viewName, HttpStatus status) {
		this.view = viewName;
		this.status = status;
	}


	public ModelAndView(@Nullable String viewName, @Nullable Map<String, ?> model, @Nullable HttpStatus status) {
		this.view = viewName;
		if (model != null) {
			getModelMap().addAllAttributes(model);
		}
		this.status = status;
	}


	public ModelAndView(String viewName, String modelName, Object modelObject) {
		this.view = viewName;
		addObject(modelName, modelObject);
	}


	public ModelAndView(View view, String modelName, Object modelObject) {
		this.view = view;
		addObject(modelName, modelObject);
	}



	public void setViewName(@Nullable String viewName) {
		this.view = viewName;
	}


	@Nullable
	public String getViewName() {
		return (this.view instanceof String ? (String) this.view : null);
	}


	public void setView(@Nullable View view) {
		this.view = view;
	}


	@Nullable
	public View getView() {
		return (this.view instanceof View ? (View) this.view : null);
	}


	public boolean hasView() {
		return (this.view != null);
	}


	//返回我们是否使用视图引用，例如{@code true}，如果视图已经通过名称指定，并由DispatcherServlet通过ViewResolver解析。
	public boolean isReference() {
		return (this.view instanceof String);
	}


	//返回模型映射。可能返回{@code null}。调用DispatcherServlet对模型进行评估。
	@Nullable
	protected Map<String, Object> getModelInternal() {
		return this.model;
	}


	public ModelMap getModelMap() {
		if (this.model == null) {
			this.model = new ModelMap();
		}
		return this.model;
	}


	public Map<String, Object> getModel() {
		return getModelMap();
	}


	public void setStatus(@Nullable HttpStatus status) {
		this.status = status;
	}


	@Nullable
	public HttpStatus getStatus() {
		return this.status;
	}



	//向模型添加属性。---即要在视图显示的数据
	public ModelAndView addObject(String attributeName, @Nullable Object attributeValue) {
		getModelMap().addAttribute(attributeName, attributeValue);
		return this;
	}


	public ModelAndView addObject(Object attributeValue) {
		getModelMap().addAttribute(attributeValue);
		return this;
	}


	public ModelAndView addAllObjects(@Nullable Map<String, ?> modelMap) {
		getModelMap().addAllAttributes(modelMap);
		return this;
	}



	//清除此ModelAndView对象的状态。
	//该对象将在以后为空。
	public void clear() {
		this.view = null;
		this.model = null;
		this.cleared = true;
	}


	//返回该ModelAndView对象是否为空，即它是否不包含任何视图，也不包含模型。
	public boolean isEmpty() {
		return (this.view == null && CollectionUtils.isEmpty(this.model));
	}


	public boolean wasCleared() {
		return (this.cleared && isEmpty());
	}



	//返回关于此模型和视图的诊断信息。
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ModelAndView: ");
		if (isReference()) {
			sb.append("reference to view with name '").append(this.view).append("'");
		} else {
			sb.append("materialized View is [").append(this.view).append(']');
		}
		sb.append("; model is ").append(this.model);
		return sb.toString();
	}

}
