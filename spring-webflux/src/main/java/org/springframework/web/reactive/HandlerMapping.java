
package org.springframework.web.reactive;

import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;

/**
 * HandlerMapping：spring mvc模块的核心组件，用于请求映射到实际的处理程序的URL解析，返回HandlerExecutionChain执行链
 * 请求映射到实际的处理程序：就是用户请求的URL，进行解析URL，根据用户请求的URL来找到相应的Controller中的方法。
 * <p>
 * 将请求与拦截器列表一起映射到处理程序，以进行预处理和后期处理。映射基于某些标准，具体标准因HandlerMapping 实现而异。
 * <p>
 * 两个主要HandlerMapping实现：
 * （1）、RequestMappingHandlerMapping （支持带@RequestMapping注释的方法）
 * （2）、SimpleUrlHandlerMapping （维护对处理程序的URI路径模式的显式注册）。
 */
public interface HandlerMapping {

	//最佳匹配的处理处程序类名称
	String BEST_MATCHING_HANDLER_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingHandler";

	//处理程序映射中包含最佳匹配模式。
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";

	/*
	 *映射处理器
	 */
	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";

	//URI属性
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";

	/**
	 * Name of the {@link ServerWebExchange#getAttributes() attribute} that
	 * contains a map with URI variable names and a corresponding MultiValueMap
	 * of URI matrix variables for each.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations and may also not be present depending on
	 * whether the HandlerMapping is configured to keep matrix variable content
	 * in the request URI.
	 */
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";

	/**
	 * Name of the {@link ServerWebExchange#getAttributes() attribute} containing
	 * the set of producible MediaType's applicable to the mapped handler.
	 * <p>Note: This attribute is not required to be supported by all
	 * HandlerMapping implementations. Handlers should not necessarily expect
	 * this request attribute to be present in all scenarios.
	 */
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";


	/*
	 *返回此请求的处理程序。
	 */
	Mono<Object> getHandler(ServerWebExchange exchange);

}
