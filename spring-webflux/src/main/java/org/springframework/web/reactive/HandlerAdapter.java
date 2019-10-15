
package org.springframework.web.reactive;

import java.util.function.Function;

import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;

/**
 * 帮助DispatcherServlet调用映射到请求的处理程序，而不管实际如何调用处理程序。
 * 例如，调用带注释的控制器需要解析注释。
 * HandlerAdapter的主要目的是保护DispatcherServlet不受这些细节的影响。
 *
 * @since 5.0
 */
public interface HandlerAdapter {

	/**
	 * Whether this {@code HandlerAdapter} supports the given {@code handler}.
	 *
	 * @param handler handler object to check
	 * @return whether or not the handler is supported
	 */
	boolean supports(Object handler);

	/**
	 * Handle the request with the given handler.
	 * <p>Implementations are encouraged to handle exceptions resulting from the
	 * invocation of a handler in order and if necessary to return an alternate
	 * result that represents an error response.
	 * <p>Furthermore since an async {@code HandlerResult} may produce an error
	 * later during result handling implementations are also encouraged to
	 * {@link HandlerResult#setExceptionHandler(Function) set an exception
	 * handler} on the {@code HandlerResult} so that may also be applied later
	 * after result handling.
	 *
	 * @param exchange current server exchange
	 * @param handler  the selected handler which must have been previously
	 *                 checked via {@link #supports(Object)}
	 * @return {@link Mono} that emits a single {@code HandlerResult} or none if
	 * the request has been fully handled and doesn't require further handling.
	 */
	Mono<HandlerResult> handle(ServerWebExchange exchange, Object handler);

}
