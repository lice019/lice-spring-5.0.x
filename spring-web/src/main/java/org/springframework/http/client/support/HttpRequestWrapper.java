
package org.springframework.http.client.support;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * 提供一个方便的{@link HttpRequest}接口实现，可以重写该接口以适应请求。
 * 这些方法默认调用已包装的请求对象。
 * HttpRequestWrapper：使用了包装模式将HTTPRequest进行包装
 */
public class HttpRequestWrapper implements HttpRequest {

	//HTTP请求对象，可以将HttpRequest子类或实现传入，然后对该对象进行包装
	private final HttpRequest request;


	//HttpRequestWrapper构造器，对HttpRequest进行传入，使用了接口的多态特性。
	//可以传入HttpRequest的实现类或子接口
	public HttpRequestWrapper(HttpRequest request) {
		Assert.notNull(request, "HttpRequest must not be null");
		this.request = request;
	}


	//返回包装后的Request
	public HttpRequest getRequest() {
		return this.request;
	}

	//返回包装后的Request对象的方法
	@Override
	@Nullable
	public HttpMethod getMethod() {
		return this.request.getMethod();
	}


	@Override
	public String getMethodValue() {
		return this.request.getMethodValue();
	}


	@Override
	public URI getURI() {
		return this.request.getURI();
	}


	@Override
	public HttpHeaders getHeaders() {
		return this.request.getHeaders();
	}

}
