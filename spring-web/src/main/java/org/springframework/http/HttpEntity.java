
package org.springframework.http;

import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * 表示由头和正文组成的HTTP请求或响应实体。
 * @param <T>
 */
public class HttpEntity<T> {

	//空的{@code HttpEntity}，没有正文或标题。
	public static final HttpEntity<?> EMPTY = new HttpEntity<>();

	//Http请求头
	private final HttpHeaders headers;

	//Http的请求体body信息
	@Nullable
	private final T body;


	//HttpEntity构造器
	protected HttpEntity() {
		this(null, null);
	}


	public HttpEntity(T body) {
		this(body, null);
	}


	public HttpEntity(MultiValueMap<String, String> headers) {
		this(null, headers);
	}


	public HttpEntity(@Nullable T body, @Nullable MultiValueMap<String, String> headers) {
		this.body = body;
		HttpHeaders tempHeaders = new HttpHeaders();
		if (headers != null) {
			tempHeaders.putAll(headers);
		}
		this.headers = HttpHeaders.readOnlyHttpHeaders(tempHeaders);
	}


	//返回Http请求实体--存储请求头的信息实体
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	//返回请求体的实体对象
	@Nullable
	public T getBody() {
		return this.body;
	}


	public boolean hasBody() {
		return (this.body != null);
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || other.getClass() != getClass()) {
			return false;
		}
		HttpEntity<?> otherEntity = (HttpEntity<?>) other;
		return (ObjectUtils.nullSafeEquals(this.headers, otherEntity.headers) &&
				ObjectUtils.nullSafeEquals(this.body, otherEntity.body));
	}

	@Override
	public int hashCode() {
		return (ObjectUtils.nullSafeHashCode(this.headers) * 29 + ObjectUtils.nullSafeHashCode(this.body));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		if (this.body != null) {
			builder.append(this.body);
			builder.append(',');
		}
		builder.append(this.headers);
		builder.append('>');
		return builder.toString();
	}

}
