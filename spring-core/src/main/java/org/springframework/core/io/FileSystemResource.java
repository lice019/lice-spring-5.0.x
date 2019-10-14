
package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardOpenOption;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link Resource}实现{@代码java. Io.Fix}句柄。支持解析为{@code file}和{@code url}。实现扩展的
 * {@link Writable Resource}接口。
 *
 * 注意：在Spring框架5中，这个{资源链接}实现使用了用于读写交互的2个API。然而，与{@link PathResource}相反，它主要管理{@代码java. IO文件}句柄。
 *
 * @see PathResource
 * @see java.io.File
 * @see java.nio.file.Files
 */
public class FileSystemResource extends AbstractResource implements WritableResource {

	//java的文件对象File
	private final File file;

	//文件路径
	private final String path;


	//传入一个File，初始化FileSystemResource
	public FileSystemResource(File file) {
		Assert.notNull(file, "File must not be null");
		this.file = file;
		//获取file的文件路径
		this.path = StringUtils.cleanPath(file.getPath());
	}

	//传入一个文件路径，初始化FileSystemResource
	public FileSystemResource(String path) {
		Assert.notNull(path, "Path must not be null");
		this.file = new File(path);
		this.path = StringUtils.cleanPath(path);
	}



	public final String getPath() {
		return this.path;
	}


	@Override
	public boolean exists() {
		return this.file.exists();
	}


	@Override
	public boolean isReadable() {
		//文件时可读的，该文件不是目录
		return (this.file.canRead() && !this.file.isDirectory());
	}


	//开启输入流
	@Override
	public InputStream getInputStream() throws IOException {
		try {
			//NIO的Files对象创建一个输入流
			return Files.newInputStream(this.file.toPath());
		}
		catch (NoSuchFileException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}


	@Override
	public boolean isWritable() {
		return (this.file.canWrite() && !this.file.isDirectory());
	}

	//通过NIO获取一个输出流
	@Override
	public OutputStream getOutputStream() throws IOException {
		return Files.newOutputStream(this.file.toPath());
	}


	@Override
	public URL getURL() throws IOException {
		return this.file.toURI().toURL();
	}


	@Override
	public URI getURI() throws IOException {
		return this.file.toURI();
	}

	//判断是否为文件
	@Override
	public boolean isFile() {
		return true;
	}


	@Override
	public File getFile() {
		return this.file;
	}

	//可读的文件管道
	@Override
	public ReadableByteChannel readableChannel() throws IOException {
		try {
			//FileChannel文件通道开启
			return FileChannel.open(this.file.toPath(), StandardOpenOption.READ);
		}
		catch (NoSuchFileException ex) {
			throw new FileNotFoundException(ex.getMessage());
		}
	}


	@Override
	public WritableByteChannel writableChannel() throws IOException {
		return FileChannel.open(this.file.toPath(), StandardOpenOption.WRITE);
	}


	@Override
	public long contentLength() throws IOException {
		return this.file.length();
	}


	@Override
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return new FileSystemResource(pathToUse);
	}


	@Override
	public String getFilename() {
		return this.file.getName();
	}


	@Override
	public String getDescription() {
		return "file [" + this.file.getAbsolutePath() + "]";
	}



	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof FileSystemResource &&
				this.path.equals(((FileSystemResource) other).path)));
	}


	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

}
