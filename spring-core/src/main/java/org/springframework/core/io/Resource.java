
package org.springframework.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.springframework.lang.Nullable;

/**
 * 从基础资源的实际类型（如文件或类路径资源）抽象的资源描述符的接口。
 * <P>一个输入流，如果它以物理形式存在，可以为每个资源打开，但是URL或文件句柄只能返回某些资源。
 * 实际行为是特定于实现的。
 *
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see UrlResource
 * @see FileUrlResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see ByteArrayResource
 * @see InputStreamResource
 */
//Resource 接 口 抽 象 了 所 有 Spring 内 部 使 用 到 的 底 层 资 源： File、 URL、 Classpath 等。
public interface Resource extends InputStreamSource {

	//确定该资源是否以物理形式存在。
	boolean exists();

	//是否为可读的
	default boolean isReadable() {
		return true;
	}

	//指示此资源是否表示具有打开流的句柄。如果{@代码true}，输入流不能多次读取，并且必须被读取和关闭以避免资源泄漏。<典型的资源描述符> p>将是{@代码false }。
	default boolean isOpen() {
		return false;
	}

	//是否为文件
	default boolean isFile() {
		return false;
	}

	//获取URL
	URL getURL() throws IOException;

	//获取URI
	URI getURI() throws IOException;

	//获取物理资源的FIle对象
	File getFile() throws IOException;

	//可读的字节码管道
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	//输入的内存长度
	long contentLength() throws IOException;

	//最新的修改
	long lastModified() throws IOException;

	//创建相对路径的Resource
	Resource createRelative(String relativePath) throws IOException;

	//获取文件的名称
	@Nullable
	String getFilename();

	//获取描述
	String getDescription();

}
