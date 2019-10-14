
package org.springframework.core.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * ClassPathResource：通过ClassLoader来加载xml配置资源
 * BeanFactory bf = new XmlBeanFactory( new ClassPathResource(" beanFactoryTest.xml"));
 * Spring 的配置文件读取是通过 ClassPathResource 进行封装的，如new ClassPathResource (" beanFactoryTest.xml")，
 *
 * Resource resource = new ClassPathResource(“ beanFactoryTest.xml”);
 *InputStream inputStream = resource.getInputStream();
 *
 * 得到inputStream后，我们就可以按照以前的开发方式进行实现了，并且我们已经可以利用 Resource及其子类
 * 为我们提供好的诸多特性。有了Resource接口便可以对所有资源文件进行统一处理。
 *
 *
 *
 * @see ClassLoader#getResourceAsStream(String)
 * @see Class#getResourceAsStream(String)
 * @since 28.12.2003
 */
public class ClassPathResource extends AbstractFileResolvingResource {

	//资源XML配置的路径，是类路径下加载
	private final String path;

	//类加载器
	@Nullable
	private ClassLoader classLoader;

	//Class字节码
	@Nullable
	private Class<?> clazz;


	//创建ClassPathResource实例，加载类路径的XML配置文件
	//new XmlBeanFactory( new ClassPathResource(" beanFactoryTest.xml"));
	public ClassPathResource(String path) {
		this(path, (ClassLoader) null);
	}


	public ClassPathResource(String path, @Nullable ClassLoader classLoader) {
		Assert.notNull(path, "Path must not be null");
		String pathToUse = StringUtils.cleanPath(path);
		if (pathToUse.startsWith("/")) {
			pathToUse = pathToUse.substring(1);
		}
		this.path = pathToUse;
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
	}

	//为{@code Class}的使用创建一个新的{@code ClassPathResource}。路径可以相对于给定的类，也可以通过一个前导斜线在类路径中是绝对的。
	public ClassPathResource(String path, @Nullable Class<?> clazz) {
		Assert.notNull(path, "Path must not be null");
		this.path = StringUtils.cleanPath(path);
		this.clazz = clazz;
	}


	@Deprecated
	protected ClassPathResource(String path, @Nullable ClassLoader classLoader, @Nullable Class<?> clazz) {
		this.path = StringUtils.cleanPath(path);
		this.classLoader = classLoader;
		this.clazz = clazz;
	}


	//返回此资源的路径(作为类路径中的资源路径)。
	public final String getPath() {
		return this.path;
	}


	@Nullable
	public final ClassLoader getClassLoader() {
		return (this.clazz != null ? this.clazz.getClassLoader() : this.classLoader);
	}



	@Override
	public boolean exists() {
		return (resolveURL() != null);
	}

	//解析基础类路径资源的URL。
	//在 Java 中， 将 不 同 来 源 的 资 源 抽 象 成 URL，
	@Nullable
	protected URL resolveURL() {
		if (this.clazz != null) {
			return this.clazz.getResource(this.path);
		} else if (this.classLoader != null) {
			return this.classLoader.getResource(this.path);
		} else {
			return ClassLoader.getSystemResource(this.path);
		}
	}

	//获取输入流
	@Override
	public InputStream getInputStream() throws IOException {
		InputStream is;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		} else if (this.classLoader != null) {
			is = this.classLoader.getResourceAsStream(this.path);
		} else {
			is = ClassLoader.getSystemResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException(getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}


	@Override
	public URL getURL() throws IOException {
		URL url = resolveURL();
		if (url == null) {
			throw new FileNotFoundException(getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}


	@Override
	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return (this.clazz != null ? new ClassPathResource(pathToUse, this.clazz) :
				new ClassPathResource(pathToUse, this.classLoader));
	}


	@Override
	@Nullable
	public String getFilename() {
		return StringUtils.getFilename(this.path);
	}


	@Override
	public String getDescription() {
		StringBuilder builder = new StringBuilder("class path resource [");
		String pathToUse = path;
		if (this.clazz != null && !pathToUse.startsWith("/")) {
			builder.append(ClassUtils.classPackageAsResourcePath(this.clazz));
			builder.append('/');
		}
		if (pathToUse.startsWith("/")) {
			pathToUse = pathToUse.substring(1);
		}
		builder.append(pathToUse);
		builder.append(']');
		return builder.toString();
	}



	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ClassPathResource)) {
			return false;
		}
		ClassPathResource otherRes = (ClassPathResource) other;
		return (this.path.equals(otherRes.path) &&
				ObjectUtils.nullSafeEquals(this.classLoader, otherRes.classLoader) &&
				ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
	}


	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

}
