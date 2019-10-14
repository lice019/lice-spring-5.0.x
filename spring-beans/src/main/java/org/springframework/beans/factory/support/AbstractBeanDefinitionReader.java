package org.springframework.beans.factory.support;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * AbstractBeanDefinitionReader：bean定义的读取器的抽象类，实现了BeanDefinitionReader，并对bean读取器进行扩展
 * AbstractBeanDefinitionReader： 对 EnvironmentCapable、 BeanDefinitionReader 类 定 义 的 功 能 进 行 实 现。
 *
 */
public abstract class AbstractBeanDefinitionReader implements EnvironmentCapable, BeanDefinitionReader {

	protected final Log logger = LogFactory.getLog(getClass());

	//Bean定义的注册中心
	private final BeanDefinitionRegistry registry;

	//ResourceLoader：定义资源加载器，主要应用于根据给定的资源文件地址回对应的 Resource。
	@Nullable
	private ResourceLoader resourceLoader;

	//类加载器
	@Nullable
	private ClassLoader beanClassLoader;

	//环境对象类
	private Environment environment;

	//bean名称生成器
	private BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		//确定要使用的ResourceLoader。
		if (this.registry instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.registry;
		} else {
			//确定resourceLoader为路径匹配资源规则的解析器PathMatchingResourcePatternResolver
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}

		// Inherit Environment if possible
		//如果可能，继承环境
		if (this.registry instanceof EnvironmentCapable) {
			this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
		} else {
			this.environment = new StandardEnvironment();
		}
	}


	//获取bean定义的注册中心对象
	public final BeanDefinitionRegistry getBeanFactory() {
		return this.registry;
	}

	//在实例化BeanDefinitionReader时候会将BeanDefinitionRegistry传入，默认使用继承自DefaultListableBeanFactory的子类
	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	//通过传参，设置资源加载器
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	@Nullable
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	//设置bean的类加载器
	public void setBeanClassLoader(@Nullable ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	@Override
	@Nullable
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	//设置读取bean定义时使用的环境。最常用于评估概要信息，以确定应该读取哪些bean定义，哪些定义应该省略。
	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
	}

	@Override
	public Environment getEnvironment() {
		return this.environment;
	}

	//设置bean的名称生成器
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new DefaultBeanNameGenerator());
	}

	@Override
	public BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}


	//根据特定的资源加载bean的定义
	@Override
	public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
		Assert.notNull(resources, "Resource array must not be null");
		int counter = 0;
		for (Resource resource : resources) {
			counter += loadBeanDefinitions(resource);
		}
		return counter;
	}

	@Override
	public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(location, null);
	}


	public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException(
					"Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver) {
			// Resource pattern matching available.
			try {
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				int loadCount = loadBeanDefinitions(resources);
				if (actualResources != null) {
					for (Resource resource : resources) {
						actualResources.add(resource);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
				}
				return loadCount;
			} catch (IOException ex) {
				throw new BeanDefinitionStoreException(
						"Could not resolve bean definition resource pattern [" + location + "]", ex);
			}
		} else {
			// Can only load single resources by absolute URL.
			Resource resource = resourceLoader.getResource(location);
			int loadCount = loadBeanDefinitions(resource);
			if (actualResources != null) {
				actualResources.add(resource);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
			}
			return loadCount;
		}
	}

	@Override
	public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int counter = 0;
		for (String location : locations) {
			counter += loadBeanDefinitions(location);
		}
		return counter;
	}

}
