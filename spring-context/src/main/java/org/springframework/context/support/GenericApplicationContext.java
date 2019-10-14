
package org.springframework.context.support;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 *GenericApplicationContext：是为通用目的设计的，它能加载各种配置文件，例如xml，properties等等。它的内部持有一个DefaultListableBeanFactory的实例，实现了BeanDefinitionRegistry接口，以便允许向其应用任何bean的定义的读取器。为了能够注册bean的定义，refresh()只允许调用一次。
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.2
 * @see #registerBeanDefinition
 * @see #refresh()
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see org.springframework.beans.factory.support.PropertiesBeanDefinitionReader
 */
public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

	private final DefaultListableBeanFactory beanFactory;

	@Nullable
	private ResourceLoader resourceLoader;

	private boolean customClassLoader = false;

	private final AtomicBoolean refreshed = new AtomicBoolean();



	//初始化GenericApplicationContext时，创建DefaultListableBeanFactory(bean集合工厂)对象，bean工厂
	public GenericApplicationContext() {
		this.beanFactory = new DefaultListableBeanFactory();
	}

	//初始化GenericApplicationContext，并传入DefaultListableBeanFactory工厂
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		this.beanFactory = beanFactory;
	}


	public GenericApplicationContext(@Nullable ApplicationContext parent) {
		this();
		setParent(parent);
	}

	//创建一个GenericApplicationContext同时，初始一个DefaultListableBeanFactory工厂
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		//初始化一个DefaultListableBeanFactory
		this(beanFactory);
		//父类
		setParent(parent);
	}


	//设置此应用程序上下文的父类，也相应地设置内部BeanFactory的父类。
	@Override
	public void setParent(@Nullable ApplicationContext parent) {
		super.setParent(parent);
		this.beanFactory.setParentBeanFactory(getInternalParentBeanFactory());
	}


	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.beanFactory.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
	}


	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.beanFactory.setAllowCircularReferences(allowCircularReferences);
	}


	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}


	//---------------------------------------------------------------------
	// ResourceLoader / ResourcePatternResolver override if necessary
	//---------------------------------------------------------------------

	//获取类引用
	@Override
	public Resource getResource(String location) {
		if (this.resourceLoader != null) {
			return this.resourceLoader.getResource(location);
		}
		return super.getResource(location);
	}

	//获取所有的类引用
	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		if (this.resourceLoader instanceof ResourcePatternResolver) {
			return ((ResourcePatternResolver) this.resourceLoader).getResources(locationPattern);
		}
		return super.getResources(locationPattern);
	}

	@Override
	public void setClassLoader(@Nullable ClassLoader classLoader) {
		super.setClassLoader(classLoader);
		this.customClassLoader = true;
	}

	@Override
	@Nullable
	public ClassLoader getClassLoader() {
		if (this.resourceLoader != null && !this.customClassLoader) {
			return this.resourceLoader.getClassLoader();
		}
		return super.getClassLoader();
	}


	//---------------------------------------------------------------------
	// Implementations of AbstractApplicationContext's template methods
	//---------------------------------------------------------------------

	//什么也不做:我们持有一个内部BeanFactory，并依靠调用者通过我们的公共方法(或BeanFactory的)注册bean。
	@Override
	protected final void refreshBeanFactory() throws IllegalStateException {
		if (!this.refreshed.compareAndSet(false, true)) {
			throw new IllegalStateException(
					"GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
		}
		this.beanFactory.setSerializationId(getId());
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		this.beanFactory.setSerializationId(null);
		super.cancelRefresh(ex);
	}

	//没什么可做的:我们有一个永远不会被释放的内部bean工厂。
	@Override
	protected final void closeBeanFactory() {
		this.beanFactory.setSerializationId(null);
	}

	//获取一个bean工厂
	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	//获取DefaultListableBeanFactory
	public final DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return this.beanFactory;
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		assertBeanFactoryActive();
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry----实现BeanDefinitionRegistry方法
	//---------------------------------------------------------------------

	//注册bean定义对象
	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	//移除bean定义对象
	@Override
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		this.beanFactory.removeBeanDefinition(beanName);
	}

	//获取bean定义实例
	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		return this.beanFactory.getBeanDefinition(beanName);
	}

	@Override
	public boolean isBeanNameInUse(String beanName) {
		return this.beanFactory.isBeanNameInUse(beanName);
	}

	@Override
	public void registerAlias(String beanName, String alias) {
		this.beanFactory.registerAlias(beanName, alias);
	}

	@Override
	public void removeAlias(String alias) {
		this.beanFactory.removeAlias(alias);
	}

	@Override
	public boolean isAlias(String beanName) {
		return this.beanFactory.isAlias(beanName);
	}


	//---------------------------------------------------------------------
	// Convenient methods for registering individual beans--注册单个bean的方便方法
	//---------------------------------------------------------------------

	//注册单个bean实例
	public final <T> void registerBean(Class<T> beanClass, BeanDefinitionCustomizer... customizers) {
		registerBean(null, beanClass, null, customizers);
	}

	//注册单个bean
	public final <T> void registerBean(
			@Nullable String beanName, Class<T> beanClass, BeanDefinitionCustomizer... customizers) {

		registerBean(beanName, beanClass, null, customizers);
	}

	//注册单个bean
	public final <T> void registerBean(
			Class<T> beanClass, Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

		registerBean(null, beanClass, supplier, customizers);
	}

	//实现单个bean的注册，是spring5.0的方法
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
			@Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {
		//获取bean构建类
		BeanDefinitionBuilder builder = (supplier != null ?
				BeanDefinitionBuilder.genericBeanDefinition(beanClass, supplier) :
				BeanDefinitionBuilder.genericBeanDefinition(beanClass));
		//将给定的自定义器应用于底层bean定义
		BeanDefinition beanDefinition = builder.applyCustomizers(customizers).getRawBeanDefinition();
		//如果beanName为空，则去class字节码的类名
		String nameToUse = (beanName != null ? beanName : beanClass.getName());
		//注册单个bean定义对象
		registerBeanDefinition(nameToUse, beanDefinition);
	}

}
