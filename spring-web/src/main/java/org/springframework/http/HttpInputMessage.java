
package org.springframework.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * 表示一个HTTP输入消息，由{@linkplain # getheader()头}和一个可读的{@linkplain #getBody() body}组成。
 * 通常由服务器端上的HTTP请求句柄或客户端上的HTTP响应句柄实现。
 *
 */
public interface HttpInputMessage extends HttpMessage {

	//将消息体作为输入流返回
	//返回HTTP的请求体body
	InputStream getBody() throws IOException;

}
