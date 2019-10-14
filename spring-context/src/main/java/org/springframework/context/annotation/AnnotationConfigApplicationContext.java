package org.springframework.context.annotation;

import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


/**
 * AnnotationConfigApplicationContext：spring通过该类时注解的开发方式初始化bean容器的入口类。
 * AnnotationConfigApplicationContext是spring3.0之后引入专门处理spring注解。
 * AnnotationConfigApplicationContext：该上下文是用于注解配置的初始化容器上下文，并管理注解的bean。AnnotationConfigApplicationContext是Spring用来加载注解配置的ApplicationContext，
 * 而ClassPathXmlApplicationContext是XML配置方式的解析的上下文
 */
public class AnnotationConfigApplicationContext extends GenericApplicationContext implements AnnotationConfigRegistry {

	//注解的bean读取器
	private final AnnotatedBeanDefinitionReader reader;

	//类路径下的bean扫描器
	private final ClassPathBeanDefinitionScanner scanner;


	//AnnotationConfigApplicationContext构造器，实例化AnnotatedBeanDefinitionReader和ClassPathBeanDefinitionScanner
	public AnnotationConfigApplicationContext() {
		//bean的读取器
		this.reader = new AnnotatedBeanDefinitionReader(this);
		//bean的扫描器
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}


	public AnnotationConfigApplicationContext(DefaultListableBeanFactory beanFactory) {
		//调用父类GenericApplicationContext的构造器，初始化一个bean工厂DefaultListableBeanFactory
		super(beanFactory);
		this.reader = new AnnotatedBeanDefinitionReader(this);
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}


	//初始化AnnotationConfigApplicationContext
	public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
		//初始化bean的读取器和扫描器
		//调用父类GenericApplicationContext无参构造函数，初始化一个BeanFactory: DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory()
		this();
		//2.注册bean配置类，new AnnotationConfigApplicationContext(AppConfig.class);
		//Appconfig.class--java形式的注解配置类,，实际是个代理对象Proxy
		register(annotatedClasses);
		//3.刷新上下文
		//refresh方法在AbstractApplicationContext容器中实现，
		// refresh()方法的作用加载或者刷新当前的配置信息，如果已经存在spring容器，则先销毁之前的容器，
		// 重新创建spring容器，载入bean定义，完成容器初始化工作，所以可以看出AnnotationConfigApplicationContext容器是通过调用其父类AbstractApplicationContext的refresh()函数启动整个IoC容器完成对Bean定义的载入。
		refresh();
	}

	/**
	 * 创建一个新的AnnotationConfigApplicationContext，扫描给定包中的bean定义并自动刷新上下文。
	 *
	 * @param basePackages the packages to check for annotated classes
	 */
	//AnnotationConfigApplicationContext初始化，并扫描basePackage包下的所有bean(使用注解标明为bean的类)
	public AnnotationConfigApplicationContext(String... basePackages) {
		this();
		//扫描basePackage包下的bean
		scan(basePackages);
		//刷新上下文容器环境
		refresh();
	}



	@Override
	public void setEnvironment(ConfigurableEnvironment environment) {
		super.setEnvironment(environment);
		this.reader.setEnvironment(environment);
		this.scanner.setEnvironment(environment);
	}


	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.reader.setBeanNameGenerator(beanNameGenerator);
		this.scanner.setBeanNameGenerator(beanNameGenerator);
		getBeanFactory().registerSingleton(
				AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
	}


	public void setScopeMetadataResolver(ScopeMetadataResolver scopeMetadataResolver) {
		this.reader.setScopeMetadataResolver(scopeMetadataResolver);
		this.scanner.setScopeMetadataResolver(scopeMetadataResolver);
	}


	//---------------------------------------------------------------------
	// Implementation of AnnotationConfigRegistry--实现AnnotationConfigRegistry
	//---------------------------------------------------------------------


	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		//注册带注解的bean
		this.reader.register(annotatedClasses);
	}


	//扫描basePackages包下带注解的类
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		//使用ClassPathBeanDefinitionScanner的scan()方法进行扫描
		this.scanner.scan(basePackages);
	}


	//---------------------------------------------------------------------
	// Convenient methods for registering individual beans--注册单个bean的方便方法
	//---------------------------------------------------------------------


	//spring5.0之后使用注解的bean进行注册
	public <T> void registerBean(Class<T> annotatedClass, Object... constructorArguments) {
		registerBean(null, annotatedClass, constructorArguments);
	}


	public <T> void registerBean(@Nullable String beanName, Class<T> annotatedClass, Object... constructorArguments) {
		this.reader.doRegisterBean(annotatedClass, null, beanName, null,
				bd -> {
					for (Object arg : constructorArguments) {
						bd.getConstructorArgumentValues().addGenericArgumentValue(arg);
					}
				});
	}

	@Override
	public <T> void registerBean(@Nullable String beanName, Class<T> beanClass, @Nullable Supplier<T> supplier,
								 BeanDefinitionCustomizer... customizers) {

		this.reader.doRegisterBean(beanClass, supplier, beanName, null, customizers);
	}

}
