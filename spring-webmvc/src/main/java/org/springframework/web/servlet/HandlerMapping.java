
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;

/**
 * 接口由定义之间的映射的对象实现,请求和处理程序对象。
 *
 *
 *
 * 这个类可以由应用程序开发人员实现，虽然这不是必须的，如{@link org.springframework.web.servlet.handler。BeanNameUrlHandlerMapping}和{@linkorg.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping}包含在框架中。
 * 如果在应用程序上下文中没有注册HandlerMapping bean，则默认为前者。
 *
 * HandlerMapping实现可以支持映射的拦截器，但是不需要。处理程序将始终包装在{@link HandlerExecutionChain}实例中，还可以选择附带一些{@link HandlerInterceptor}实例。
 * DispatcherServlet将首先按照给定的顺序调用每个HandlerInterceptor的{@code preHandle}方法，最后调用处理程序本身，如果所有{@code preHandle}方法都返回{@code true}。
 *
 * 参数化映射的能力是这个MVC框架的一个强大的和不寻常的功能。例如，可以编写基于会话状态、cookie状态或其他许多状态的自定义映射
 * 变量。似乎没有其他MVC框架具有同样的灵活性。
 *
 * 注意:实现可以实现{@link org.springframework.core。接口能够指定排序顺序，从而指定DispatcherServlet应用的优先级。无序实例被视为最低优先级。
 *
 * @see org.springframework.core.Ordered
 * @see org.springframework.web.servlet.handler.AbstractHandlerMapping
 * @see org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping
 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
 */
public interface HandlerMapping {


	//包含最佳匹配模式的映射处理程序的{@link HttpServletRequest}属性的名称。
	String BEST_MATCHING_HANDLER_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingHandler";


	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";


	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";


	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";


	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";


	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";


	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";


	@Nullable
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
