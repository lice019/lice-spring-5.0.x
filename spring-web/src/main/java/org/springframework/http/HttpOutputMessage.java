package org.springframework.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 响应HTTP请求向服务端或客服端输出信息
 */
public interface HttpOutputMessage extends HttpMessage {

	//通过输出流获取消息
	OutputStream getBody() throws IOException;

}
