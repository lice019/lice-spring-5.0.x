
package org.springframework.http;

import java.net.URI;

import org.springframework.lang.Nullable;

/**
 * HttpRequest:代表HTTP客户端的请求信息，包含方法、方法值、URI
 */
public interface HttpRequest extends HttpMessage {

	//
	@Nullable
	default HttpMethod getMethod() {
		return HttpMethod.resolve(getMethodValue());
	}

	//获取HTTP请求Request的方法值
	String getMethodValue();

	//获取该HTTP的Request的URI
	URI getURI();

}
