package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;



/**
 * AnnotatedBeanDefinitionReader:是一个单一的类，并没有继承任何关系。AnnotatedBeanDefinitionReader的主要作用是读取.class文件的配置，而ClassPathBeanDefinitionScanner是扫描作用bean注解的扫描器
 * AnnotatedBeanDefinitionReader主要是读取AppConfig.class配置类中配置的@Bean，他没有扫描作用
 *@Configuration
 *@ComponentScan("com.lice")
 * public class AppConfig {
 *
 *    @Bean
 *    public Student student(){
 * 		return new Student();
 *    }
 * }
 */
public class AnnotatedBeanDefinitionReader {

	//BeanDefinition的注册器，被bean工厂类实现，功能与bean工厂相似
	private final BeanDefinitionRegistry registry;

	//bean的名称生成器
	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	//bean的Scope解析器
	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	//候选评估者，不知有什么用
	private ConditionEvaluator conditionEvaluator;



	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, getOrCreateEnvironment(registry));
	}


	//AnnotatedBeanDefinitionReader构造器
	//传入的Environment为StandardEnvironment
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
	}



	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}


	public void setEnvironment(Environment environment) {
		this.conditionEvaluator = new ConditionEvaluator(this.registry, environment, null);
	}


	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
	}

	//设置bean的范围元数据解析器
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}


	//注册bean的配置类
	public void register(Class<?>... annotatedClasses) {
		//遍历所有的bean配置类
		for (Class<?> annotatedClass : annotatedClasses) {
			//将bean的配置类注册到bean工厂中
			registerBean(annotatedClass);
		}
	}

	//将注解类bean注册到工厂中
	public void registerBean(Class<?> annotatedClass) {
		doRegisterBean(annotatedClass, null, null, null);
	}


	public <T> void registerBean(Class<T> annotatedClass, @Nullable Supplier<T> instanceSupplier) {
		doRegisterBean(annotatedClass, instanceSupplier, null, null);
	}


	public <T> void registerBean(Class<T> annotatedClass, String name, @Nullable Supplier<T> instanceSupplier) {
		doRegisterBean(annotatedClass, instanceSupplier, name, null);
	}


	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> annotatedClass, Class<? extends Annotation>... qualifiers) {
		doRegisterBean(annotatedClass, null, null, qualifiers);
	}


	@SuppressWarnings("unchecked")
	public void registerBean(Class<?> annotatedClass, String name, Class<? extends Annotation>... qualifiers) {
		doRegisterBean(annotatedClass, null, name, qualifiers);
	}


	//annotatedClass==com.lice.config.AppConfig.class
	<T> void doRegisterBean(Class<T> annotatedClass, @Nullable Supplier<T> instanceSupplier, @Nullable String name,
							@Nullable Class<? extends Annotation>[] qualifiers, BeanDefinitionCustomizer... definitionCustomizers) {

		//将com.lice.config.AppConfig.class封装成bean
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(annotatedClass);
		//根据{@code @Conditional}注释判断一个项目是否应该被跳过。{@link ConfigurationPhase}将从项的类型(即a
		if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
			return;
		}

		abd.setInstanceSupplier(instanceSupplier);
		//解析com.lice.config.AppConfig.class中的注解
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		//设置作用范围属性
		abd.setScope(scopeMetadata.getScopeName());
		//生成bean的名称
		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

		//通过AnnotationConfigUtils来处理通用的注解bean
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				if (Primary.class == qualifier) {
					abd.setPrimary(true);
				} else if (Lazy.class == qualifier) {
					abd.setLazyInit(true);
				} else {
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}
		//遍历Bean定义编辑器
		for (BeanDefinitionCustomizer customizer : definitionCustomizers) {
			customizer.customize(abd);
		}

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
	}



	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable) {
			return ((EnvironmentCapable) registry).getEnvironment();
		}
		return new StandardEnvironment();
	}

}
