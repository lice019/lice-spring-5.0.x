
package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * ClassPathXmlApplicationContext：用于XML配置的spring上下文初始化IOC容器
 * 独立的XML应用程序上下文，包含上下文定义文件
 * 将普通路径解释为类路径资源名
 * 包括包路径的。“mypackage / myresource.txt”)。对于测试用例以及嵌入在jar中的应用程序上下文非常有用。
 * <p>
 * 配置位置的默认值可以通过{@link #getConfigLocations}来覆盖，配置位置可以表示具体的文件，比如“/myfiles/context”。xml”或ant样式的模式，如“/myfiles/*-context”。参见{@link org.springframework.util。用于模式细节的AntPathMatcher} javadoc)。
 *
 *
 * <p><b>This is a simple, one-stop shop convenience ApplicationContext.
 * Consider using the {@link GenericApplicationContext} class in combination
 * with an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}
 * for more flexible context setup.</b>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getResource
 * @see #getResourceByPath
 * @see GenericApplicationContext
 */
//ClassPathXmlApplicationContext:用于spring的XML配置的上下文
// ApplicationContext ac = new ClassPathXmlApplicationContext("classPath:applicationContext.xml");	初始化IOC容器
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	//配置文件资源
	@Nullable
	private Resource[] configResources;


	/*
	 *设置路径是必不可少的步骤， ClassPathXmlApplicationContext 中可以将配置文件路径以数组的方式传入，
	 * ClassPathXmlApplicationContext 可以对数组进行解析并进行加载。
	 * 而对于解析及功能实现都在refresh()中实现。
	 * refresh():是父类AbstractApplicationContext中方法，不管是XML配置，还是注解方法配置，解析功能都是在
	 * AbstractApplicationContext父类中进行的
	 */
	public ClassPathXmlApplicationContext() {
	}


	public ClassPathXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}


	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[]{configLocation}, true, null);
	}


	public ClassPathXmlApplicationContext(String... configLocations) throws BeansException {
		this(configLocations, true, null);
	}


	public ClassPathXmlApplicationContext(String[] configLocations, @Nullable ApplicationContext parent)
			throws BeansException {

		this(configLocations, true, parent);
	}


	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this(configLocations, refresh, null);
	}


	public ClassPathXmlApplicationContext(
			String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
			throws BeansException {

		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}


	public ClassPathXmlApplicationContext(String path, Class<?> clazz) throws BeansException {
		this(new String[]{path}, clazz);
	}


	public ClassPathXmlApplicationContext(String[] paths, Class<?> clazz) throws BeansException {
		this(paths, clazz, null);
	}


	public ClassPathXmlApplicationContext(String[] paths, Class<?> clazz, @Nullable ApplicationContext parent)
			throws BeansException {

		super(parent);
		Assert.notNull(paths, "Path array must not be null");
		Assert.notNull(clazz, "Class argument must not be null");
		this.configResources = new Resource[paths.length];
		for (int i = 0; i < paths.length; i++) {
			this.configResources[i] = new ClassPathResource(paths[i], clazz);
		}
		refresh();
	}


	@Override
	@Nullable
	protected Resource[] getConfigResources() {
		return this.configResources;
	}

}
