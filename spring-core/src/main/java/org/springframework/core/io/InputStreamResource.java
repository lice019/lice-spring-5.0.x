
package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {链接资源}实现给定的{@链接输入流}。除非其他特定的{@代码资源}实现是适用的，否则只应使用P>。尤其是{@link bytearrayresource}或者
 * <p>
 * 在可能的情况下实现基于文件的{@代码资源}实现。
 * <P>与其他{@代码资源}实现相反，这是<i>已经打开的</i>资源的描述符-因此从{@ Link SysISOPEN（）}返回{@代码真}。如果需要，请不要使用{@code inputstreamresource}
 * <p>
 * 将资源描述符保留在某个地方，或者如果需要多次从流读取。
 *
 * @see ByteArrayResource
 * @see ClassPathResource
 * @see FileSystemResource
 * @see UrlResource
 * @since 28.12.2003
 */
public class InputStreamResource extends AbstractResource {

	//输入流
	private final InputStream inputStream;

	//描述
	private final String description;

	//是否读
	private boolean read = false;



	public InputStreamResource(InputStream inputStream) {
		this(inputStream, "resource loaded through InputStream");
	}


	public InputStreamResource(InputStream inputStream, @Nullable String description) {
		Assert.notNull(inputStream, "InputStream must not be null");
		this.inputStream = inputStream;
		this.description = (description != null ? description : "");
	}



	@Override
	public boolean exists() {
		return true;
	}


	@Override
	public boolean isOpen() {
		return true;
	}


	@Override
	public InputStream getInputStream() throws IOException, IllegalStateException {
		if (this.read) {
			throw new IllegalStateException("InputStream has already been read - " +
					"do not use InputStreamResource if a stream needs to be read multiple times");
		}
		this.read = true;
		return this.inputStream;
	}


	@Override
	public String getDescription() {
		return "InputStream resource [" + this.description + "]";
	}



	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof InputStreamResource &&
				((InputStreamResource) other).inputStream.equals(this.inputStream)));
	}


	@Override
	public int hashCode() {
		return this.inputStream.hashCode();
	}

}
