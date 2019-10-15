package org.springframework.web.context.support;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigRegistry;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoader;

/**
 * AnnotationConfigWebApplicationContext：该类与AnnotationConfigApplicationContext类的作用是一样的，
 * 而AnnotationConfigApplicationContext是用于Spring的IOC容器初始化，AnnotationConfigWebApplicationContext
 * 是用于Spring 的Web容器的初始化。
 * AnnotationConfigWebApplicationContext是Spring中直接或间接继承了所有WebApplicationContext,
 * 用于初始化Servlet容器。并初始化DispatchServlet所需的HandlerMap、HandlerAdapter、ViewResolver等
 *
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public class AnnotationConfigWebApplicationContext extends AbstractRefreshableWebApplicationContext
		implements AnnotationConfigRegistry {

	//bean的名称生成器，会根据一定的规则去生成bean的名称。
	@Nullable
	private BeanNameGenerator beanNameGenerator;

	//bean的scope范围解析
	@Nullable
	private ScopeMetadataResolver scopeMetadataResolver;

	//注解配置类
	private final Set<Class<?>> annotatedClasses = new LinkedHashSet<>();

	//扫描包的字符串，来告知spring扫什么包
	private final Set<String> basePackages = new LinkedHashSet<>();


	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = beanNameGenerator;
	}


	@Nullable
	protected BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}


	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver = scopeMetadataResolver;
	}


	@Nullable
	protected ScopeMetadataResolver getScopeMetadataResolver() {
		return this.scopeMetadataResolver;
	}


	/*
	 *注册一个或多个带@Configuration注解配置类，然后去扫描包、初始化一个bean工厂DefaultListableBeanFactory，
	 * 也可以是设置XML配置文件的路径，最后调用上下文的refresh（）方法去初始化IOC。
	 * 这个方法实则是一个空壳方法，没做什么，只是将注解配置的Class字节码添加到Set集合中而已。
	 *
	 * @param annotatedClasses   一个或多个带@Configuration注解配置类，
	 * @see #scan(String...)     扫描包路径
	 * @see #loadBeanDefinitions(DefaultListableBeanFactory)  加载bean工厂
	 * @see #setConfigLocation(String)   设置XML配置路径
	 * @see #refresh()   刷新容器
	 */
	public void register(Class<?>... annotatedClasses) {
		Assert.notEmpty(annotatedClasses, "At least one annotated class must be specified");
		Collections.addAll(this.annotatedClasses, annotatedClasses);
	}

	/*
	 * 包扫描方法
	 */
	public void scan(String... basePackages) {
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		Collections.addAll(this.basePackages, basePackages);
	}


	/*
	 *
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
		//注解bean的读取器，读取@Configuration注解配置中定义@Bean的bean
		AnnotatedBeanDefinitionReader reader = getAnnotatedBeanDefinitionReader(beanFactory);
		//类路径bean的扫描器，用于扫描@Controller，@Service等注解的bean
		ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner(beanFactory);

		//bean的名称策略生成器
		BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
		//判断beanNameGenerator不为null，就设置给reader和scanner
		if (beanNameGenerator != null) {
			reader.setBeanNameGenerator(beanNameGenerator);
			scanner.setBeanNameGenerator(beanNameGenerator);
			//将bean的名称生成器以单例模式注册到IOC容器中
			beanFactory.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
		}

		//元数据解析器，用于解析注解
		ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();
		//scopeMetadataResolver不为null，就设置给reader和scanner，用于将读取和扫描到bean
		//进行解析
		if (scopeMetadataResolver != null) {
			reader.setScopeMetadataResolver(scopeMetadataResolver);
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		//判断annotatedClasses注解配置类是否为空
		if (!this.annotatedClasses.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Registering annotated classes: [" +
						StringUtils.collectionToCommaDelimitedString(this.annotatedClasses) + "]");
			}
			//将@Configuration注解配置类给bean的读取器，进行读取解析
			reader.register(ClassUtils.toClassArray(this.annotatedClasses));
		}

		//判断@ComponentScan("com.lice")中的值是否为空，空则无法根据包路径进行扫描
		if (!this.basePackages.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Scanning base packages: [" +
						StringUtils.collectionToCommaDelimitedString(this.basePackages) + "]");
			}
			//将包路径basePackages委托给scanner扫描器进行分析，去相应的包下进行扫描bean
			scanner.scan(StringUtils.toStringArray(this.basePackages));
		}

		//获取配置路径
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			//遍历每个配置路径
			for (String configLocation : configLocations) {
				try {
					//加载配置文件
					Class<?> clazz = ClassUtils.forName(configLocation, getClassLoader());
					if (logger.isInfoEnabled()) {
						logger.info("Successfully resolved class for [" + configLocation + "]");
					}
					//将配置路径类的Class字节码注册给bean的读取器
					reader.register(clazz);
				} catch (ClassNotFoundException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Could not load class for config location [" + configLocation +
								"] - trying package scan. " + ex);
					}
					//根据配置路径去扫描包下的bean，返回扫到bean的数量
					int count = scanner.scan(configLocation);
					if (logger.isInfoEnabled()) {
						if (count == 0) {
							logger.info("No annotated classes found for specified class/package [" + configLocation + "]");
						} else {
							logger.info("Found " + count + " annotated classes in package [" + configLocation + "]");
						}
					}
				}
			}
		}
	}


	//获取含有bean工厂和运行环境的AnnotatedBeanDefinitionReader读取器
	protected AnnotatedBeanDefinitionReader getAnnotatedBeanDefinitionReader(DefaultListableBeanFactory beanFactory) {
		return new AnnotatedBeanDefinitionReader(beanFactory, getEnvironment());
	}

	//获取含有bean工厂、运行环境和默认的过滤器的ClassPathBeanDefinitionScanner的bean扫描器
	protected ClassPathBeanDefinitionScanner getClassPathBeanDefinitionScanner(DefaultListableBeanFactory beanFactory) {
		return new ClassPathBeanDefinitionScanner(beanFactory, true, getEnvironment());
	}

}
