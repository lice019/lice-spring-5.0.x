
package org.springframework.http.client;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpRequest;

/**
 * 表示客户端HTTP请求。
 * 通过实现{@link ClientHttpRequestFactory}创建。
 * 一个{@code ClientHttpRequest}可以是{@linkplain #execute() execution}，
 * 接收一个可以读取的{@link ClientHttpResponse}。
 *
 * @see ClientHttpRequestFactory#createRequest(java.net.URI, HttpMethod)
 */
public interface ClientHttpRequest extends HttpRequest, HttpOutputMessage {

	//执行此请求，生成一个可以读取的{@link ClientHttpResponse}。
	ClientHttpResponse execute() throws IOException;

}
