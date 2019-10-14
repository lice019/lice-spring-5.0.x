
package org.springframework.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.Nullable;

/**
 * HTTP请求方法的Java 5枚举。用于与{@link org.springframework.http.client}一起使用。
 * 和{@link org.springframework.web.client.RestTemplate}。
 *
 * @since 3.0
 */
public enum HttpMethod {

	//Http的请求方式
	GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

	//Http的请求方式容器
	private static final Map<String, HttpMethod> mappings = new HashMap<>(16);

	//将Http的请求方式存储到HashMap中
	static {
		for (HttpMethod httpMethod : values()) {
			mappings.put(httpMethod.name(), httpMethod);
		}
	}



	@Nullable
	public static HttpMethod resolve(@Nullable String method) {
		return (method != null ? mappings.get(method) : null);
	}


	//确定这个{@code HttpMethod}是否匹配给定的方法值。
	public boolean matches(String method) {
		return (this == resolve(method));
	}

}
