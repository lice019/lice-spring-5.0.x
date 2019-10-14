
package org.springframework.http;

/**
 * 表示HTTP请求和响应消息的基本接口。由{@link HttpHeaders}组成，可通过{@link # getheader()}检索。
 *
 */
public interface HttpMessage {

	//返回HTTP请求头信息
	HttpHeaders getHeaders();

}
