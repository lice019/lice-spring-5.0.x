
package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.lang.Nullable;

/**
 * AbstractRefreshableApplicationContext：可刷新的XML配置上下文
 *
 * @see #loadBeanDefinitions
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see AbstractXmlApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	//允许bean的定义被重写
	@Nullable
	private Boolean allowBeanDefinitionOverriding;

	@Nullable
	private Boolean allowCircularReferences;

	//spring中最强大的bean工厂
	@Nullable
	private DefaultListableBeanFactory beanFactory;

	//同步监听器
	private final Object beanFactoryMonitor = new Object();


	//子类的创建的构造器
	public AbstractRefreshableApplicationContext() {
	}

	//使用给定的父上下文创建一个新的abstractrefrembleapplicationcontext。
	public AbstractRefreshableApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}


	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}


	/*
	 * 此实现实际执行此上下文的底层bean工厂的刷新，
	 * 关闭前一个bean工厂(如果有的话)并为上下文生命周期的下一阶段初始化一个新的bean工厂。
	 * 目的是为ApplicationContext提供BeanFactory的所有功能
	 */
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		//判断是否有bean工厂
		if (hasBeanFactory()) {
			//销毁bean工厂的bean实例
			destroyBeans();
			//关闭现有的bean工厂
			closeBeanFactory();
		}
		try {
			//创建DefaultListableBeanFactory
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			//设置工厂的唯一id
			beanFactory.setSerializationId(getId());
			//为用户使用的BeanFactory进行装配
			// 设置@Autowired 和 @Qualifier注解解析器 QualifierAnnotationAutowireCandidateResolver
			customizeBeanFactory(beanFactory);
			//加载bean工厂的bean实例
			/*
			 *初始化一个DocumentReader或AnnotatedBeanDefinitionReader（注解bean读取器）
			 * ClassPathBeanDefinitionScanner（bean扫描器）
			 * 该方法实现在AbstractXMLApplicationContext中
			 */
			loadBeanDefinitions(beanFactory);
			//保证bean工厂的唯一
			synchronized (this.beanFactoryMonitor) {
				this.beanFactory = beanFactory;
			}
		} catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

	//通过异常来取消刷新
	@Override
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.setSerializationId(null);
			}
		}
		super.cancelRefresh(ex);
	}

	//关闭bean工厂，实际上是将bean工厂的实例置为null
	@Override
	protected final void closeBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null) {
				this.beanFactory.setSerializationId(null);
				this.beanFactory = null;
			}
		}
	}

	//确定此上下文当前是否包含bean工厂，即至少已刷新一次且尚未关闭。S
	protected final boolean hasBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			return (this.beanFactory != null);
		}
	}

	//获取bean工厂
	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory not initialized or already closed - " +
						"call 'refresh' before accessing beans via the ApplicationContext");
			}
			return this.beanFactory;
		}
	}

	//判断bean工厂是否存活
	@Override
	protected void assertBeanFactoryActive() {
	}

	//创建bean工厂，实际上new出来一个DefaultListableBeanFactory实例
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}


	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		if (this.allowBeanDefinitionOverriding != null) {
			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		if (this.allowCircularReferences != null) {
			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
		}
	}

	//加载bean，由子类实现
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory)
			throws BeansException, IOException;

}
