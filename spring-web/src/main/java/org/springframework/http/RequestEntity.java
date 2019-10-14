package org.springframework.http;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

/**
 * 扩展了{@link HttpEntity}，添加了{@linkplain HttpMethod}和{@linkplain URI URI}。
 * 在{@code RestTemplate}和{@code @Controller}方法中使用。
 */
public class RequestEntity<T> extends HttpEntity<T> {

	//Http的Request请求方法
	@Nullable
	private final HttpMethod method;

	//请求到服务器的URL
	private final URI url;

	@Nullable
	private final Type type;


	public RequestEntity(HttpMethod method, URI url) {
		this(null, null, method, url, null);
	}


	public RequestEntity(@Nullable T body, HttpMethod method, URI url) {
		this(body, null, method, url, null);
	}


	public RequestEntity(@Nullable T body, HttpMethod method, URI url, Type type) {
		this(body, null, method, url, type);
	}


	public RequestEntity(MultiValueMap<String, String> headers, HttpMethod method, URI url) {
		this(null, headers, method, url, null);
	}


	public RequestEntity(@Nullable T body, @Nullable MultiValueMap<String, String> headers,
						 @Nullable HttpMethod method, URI url) {

		this(body, headers, method, url, null);
	}


	public RequestEntity(@Nullable T body, @Nullable MultiValueMap<String, String> headers,
						 @Nullable HttpMethod method, URI url, @Nullable Type type) {

		super(body, headers);
		this.method = method;
		this.url = url;
		this.type = type;
	}


	@Nullable
	public HttpMethod getMethod() {
		return this.method;
	}


	public URI getUrl() {
		return this.url;
	}


	@Nullable
	public Type getType() {
		if (this.type == null) {
			T body = getBody();
			if (body != null) {
				return body.getClass();
			}
		}
		return this.type;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!super.equals(other)) {
			return false;
		}
		RequestEntity<?> otherEntity = (RequestEntity<?>) other;
		return (ObjectUtils.nullSafeEquals(getMethod(), otherEntity.getMethod()) &&
				ObjectUtils.nullSafeEquals(getUrl(), otherEntity.getUrl()));
	}

	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.method);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.url);
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		builder.append(getMethod());
		builder.append(' ');
		builder.append(getUrl());
		builder.append(',');
		T body = getBody();
		HttpHeaders headers = getHeaders();
		if (body != null) {
			builder.append(body);
			builder.append(',');
		}
		builder.append(headers);
		builder.append('>');
		return builder.toString();
	}


	// Static builder methods

	//使用给定的方法和url创建一个生成器。
	//@param method the HTTP method (GET, POST, etc)

	public static BodyBuilder method(HttpMethod method, URI url) {
		return new DefaultBodyBuilder(method, url);
	}


	public static HeadersBuilder<?> get(URI url) {
		return method(HttpMethod.GET, url);
	}


	public static HeadersBuilder<?> head(URI url) {
		return method(HttpMethod.HEAD, url);
	}


	public static BodyBuilder post(URI url) {
		return method(HttpMethod.POST, url);
	}


	public static BodyBuilder put(URI url) {
		return method(HttpMethod.PUT, url);
	}


	public static BodyBuilder patch(URI url) {
		return method(HttpMethod.PATCH, url);
	}


	public static HeadersBuilder<?> delete(URI url) {
		return method(HttpMethod.DELETE, url);
	}


	public static HeadersBuilder<?> options(URI url) {
		return method(HttpMethod.OPTIONS, url);
	}



	public interface HeadersBuilder<B extends HeadersBuilder<B>> {

		/**
		 * Add the given, single header value under the given name.
		 *
		 * @param headerName   the header name
		 * @param headerValues the header value(s)
		 * @return this builder
		 * @see HttpHeaders#add(String, String)
		 */
		B header(String headerName, String... headerValues);

		/**
		 * Set the list of acceptable {@linkplain MediaType media types}, as
		 * specified by the {@code Accept} header.
		 *
		 * @param acceptableMediaTypes the acceptable media types
		 */
		B accept(MediaType... acceptableMediaTypes);

		/**
		 * Set the list of acceptable {@linkplain Charset charsets}, as specified
		 * by the {@code Accept-Charset} header.
		 *
		 * @param acceptableCharsets the acceptable charsets
		 */
		B acceptCharset(Charset... acceptableCharsets);

		/**
		 * Set the value of the {@code If-Modified-Since} header.
		 * <p>The date should be specified as the number of milliseconds since
		 * January 1, 1970 GMT.
		 *
		 * @param ifModifiedSince the new value of the header
		 */
		B ifModifiedSince(long ifModifiedSince);

		/**
		 * Set the values of the {@code If-None-Match} header.
		 *
		 * @param ifNoneMatches the new value of the header
		 */
		B ifNoneMatch(String... ifNoneMatches);

		/**
		 * Builds the request entity with no body.
		 *
		 * @return the request entity
		 * @see BodyBuilder#body(Object)
		 */
		RequestEntity<Void> build();
	}


	/**
	 * Defines a builder that adds a body to the response entity.
	 */
	public interface BodyBuilder extends HeadersBuilder<BodyBuilder> {

		/**
		 * Set the length of the body in bytes, as specified by the
		 * {@code Content-Length} header.
		 *
		 * @param contentLength the content length
		 * @return this builder
		 * @see HttpHeaders#setContentLength(long)
		 */
		BodyBuilder contentLength(long contentLength);

		/**
		 * Set the {@linkplain MediaType media type} of the body, as specified
		 * by the {@code Content-Type} header.
		 *
		 * @param contentType the content type
		 * @return this builder
		 * @see HttpHeaders#setContentType(MediaType)
		 */
		BodyBuilder contentType(MediaType contentType);

		/**
		 * Set the body of the request entity and build the RequestEntity.
		 *
		 * @param <T>  the type of the body
		 * @param body the body of the request entity
		 * @return the built request entity
		 */
		<T> RequestEntity<T> body(T body);

		/**
		 * Set the body and type of the request entity and build the RequestEntity.
		 *
		 * @param <T>  the type of the body
		 * @param body the body of the request entity
		 * @param type the type of the body, useful for generic type resolution
		 * @return the built request entity
		 * @since 4.3
		 */
		<T> RequestEntity<T> body(T body, Type type);
	}


	private static class DefaultBodyBuilder implements BodyBuilder {

		private final HttpMethod method;

		private final URI url;

		private final HttpHeaders headers = new HttpHeaders();

		public DefaultBodyBuilder(HttpMethod method, URI url) {
			this.method = method;
			this.url = url;
		}

		@Override
		public BodyBuilder header(String headerName, String... headerValues) {
			for (String headerValue : headerValues) {
				this.headers.add(headerName, headerValue);
			}
			return this;
		}

		@Override
		public BodyBuilder accept(MediaType... acceptableMediaTypes) {
			this.headers.setAccept(Arrays.asList(acceptableMediaTypes));
			return this;
		}

		@Override
		public BodyBuilder acceptCharset(Charset... acceptableCharsets) {
			this.headers.setAcceptCharset(Arrays.asList(acceptableCharsets));
			return this;
		}

		@Override
		public BodyBuilder contentLength(long contentLength) {
			this.headers.setContentLength(contentLength);
			return this;
		}

		@Override
		public BodyBuilder contentType(MediaType contentType) {
			this.headers.setContentType(contentType);
			return this;
		}

		@Override
		public BodyBuilder ifModifiedSince(long ifModifiedSince) {
			this.headers.setIfModifiedSince(ifModifiedSince);
			return this;
		}

		@Override
		public BodyBuilder ifNoneMatch(String... ifNoneMatches) {
			this.headers.setIfNoneMatch(Arrays.asList(ifNoneMatches));
			return this;
		}

		@Override
		public RequestEntity<Void> build() {
			return new RequestEntity<>(this.headers, this.method, this.url);
		}

		@Override
		public <T> RequestEntity<T> body(T body) {
			return new RequestEntity<>(body, this.headers, this.method, this.url);
		}

		@Override
		public <T> RequestEntity<T> body(T body, Type type) {
			return new RequestEntity<>(body, this.headers, this.method, this.url, type);
		}
	}

}
