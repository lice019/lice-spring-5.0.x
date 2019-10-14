package org.springframework.context.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;


/**
 * ClassPathBeanDefinitionScanner在spring-context模块中
 * ClassPathBeanDefinitionScanner有以下作用：
 * (1)、扫描类路径下的候选Component，构造BeanDefinition对象（实际为ScannedGenericBeanDefinition）
 * (2)、利用BeanDefinitionRegister注册BeanDefinition到bean工厂中；
 *
 * ClassPathBeanDefinitionScanner继承于ClassPathScanningCandidateComponentProvider，ClassPathBeanDefinitionScanner主要是在扫描符合的bean的组件，
 * 然后通过注册器将bean注册到bean工厂中。
 *
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	//BeanDefinition注册中心
	private final BeanDefinitionRegistry registry;

	//bean的简单信息封装类对象，BeanDefinitionDefaults只是简单的拓展了Object，AbstractBeanDefinition是详细的封装bean信息的父类
	private BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();

	@Nullable
	private String[] autowireCandidatePatterns;

	//注释Bean名称生成器
	private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

	//bean的作用范围解析器
	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	//是否包括注释配置
	private boolean includeAnnotationConfig = true;


	//传入一个bean注册器，初始化一个类路径扫描器
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this(registry, true);
	}

	//bean注册器、默认过滤器初始化一个类路径曹扫描器
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		this(registry, useDefaultFilters, getOrCreateEnvironment(registry));
	}

	//bean注册器、默认过滤器和运行环境初始化一个类路径曹扫描器
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment) {

		this(registry, useDefaultFilters, environment,
				(registry instanceof ResourceLoader ? (ResourceLoader) registry : null));
	}


	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment, @Nullable ResourceLoader resourceLoader) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		//设置spring运行时的环境
		setEnvironment(environment);
		//设置spring的资源解析器
		setResourceLoader(resourceLoader);
	}


	//返回此扫描器操作的BeanDefinitionRegistry。
	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}


	public void setBeanDefinitionDefaults(@Nullable BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults =
				(beanDefinitionDefaults != null ? beanDefinitionDefaults : new BeanDefinitionDefaults());
	}


	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		return this.beanDefinitionDefaults;
	}


	public void setAutowireCandidatePatterns(@Nullable String... autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}


	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new AnnotationBeanNameGenerator());
	}


	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}


	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
	}


	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}


	/**
	 * 该方法被AnnotationConfigApplicationContext类中的ClassPathBeanDefinitionScanner成员对象调用
	 * 在指定的基本包中执行扫描
	 * @param basePackages 被扫描的包名称
	 * @return number of beans registered 返回被注册的bean
	 */
	public int scan(String... basePackages) {
		//容器中未扫描前的bean数
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		//扫描指定的包
		doScan(basePackages);

		// Register annotation config processors, if necessary.--如果需要，注册注释配置处理器。
		if (this.includeAnnotationConfig) {
			//是否包括注释配置
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		//this.registry.getBeanDefinitionCount() - beanCountAtScanStart得到扫描后，新增的bean数量
		return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
	}

	/**
	 * 在指定的基本包中执行扫描，返回已注册的bean集合。
	 * <p>
	 * 该方法不注册注释配置处理器，而是将此任务留给调用者。
	 * @param basePackages 需要被扫描的包
	 * @return 返回已注册的bean集合
	 */
	//将扫描到的bean封装到BeanDefinition中，再将BeanDefinition装入到Set集合中返回。
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		//判断包名是否为空
		Assert.notEmpty(basePackages, "At least one base package must be specified");
		//new一个LinkedHashSet来存储扫描到的bean
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
		//遍历可变参数传入的包名
		for (String basePackage : basePackages) {
			//通过ClassPathScanningCandidateComponentProvider中findCandidateComponents(basePackage)方法
			//找到符合条件候选bean，并返回
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
			//取出集合中候选的bean
			for (BeanDefinition candidate : candidates) {
				//获取bean的Scope范围，单例或多例
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
				//设置到BeanDefinition中
				candidate.setScope(scopeMetadata.getScopeName());
				//获取bean的名称，通过bean名称的生成器，如果被定义，则使用名义的；如果没定义，则使用类名，第一个字母小写
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
				//bean是否是AbstractBeanDefinition的实例
				if (candidate instanceof AbstractBeanDefinition) {
					//bean定义处理器
					postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
				}
				//bean是否是AnnotatedBeanDefinition的实例
				if (candidate instanceof AnnotatedBeanDefinition) {
					//通过AnnotationConfigUtils来获取bean工厂，并处理bean通用的属性
					AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
				}
				//检查给定候选人的bean名称，确定是否需要注册对应的bean定义，或者是否与现有定义冲突。
				if (checkCandidate(beanName, candidate)) {
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
					definitionHolder =
							AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
					beanDefinitions.add(definitionHolder);
					//封装成BeanDefinitionHolder的bean注册到bean工厂中
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
		}
		//返回一个bean的集合
		return beanDefinitions;
	}

	/**
	 * 在扫描组件类检索到的内容之外，对给定bean定义应用更多设置。
	 * @param beanDefinition 被扫描到的bean
	 * @param beanName bean的名称
	 */
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		beanDefinition.applyDefaults(this.beanDefinitionDefaults);
		if (this.autowireCandidatePatterns != null) {
			beanDefinition.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
		}
	}


	//将BeanDefinition注册到registry
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}


	//检查给定候选人的bean名称，确定是否需要注册对应的bean定义，或者是否与现有定义冲突。
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}
		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
		if (originatingDef != null) {
			existingDef = originatingDef;
		}
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}
		throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
				"' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
				"non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
	}

	/**
	 * 确定给定的新bean定义是否与给定的现有bean定义兼容。
	 * 当现有bean定义来自同一源或非扫描源时，默认实现认为它们是兼容的。
	 *
	 * @param newDefinition 来自于扫描得到的新bean
	 * @param existingDefinition 已经存在的bean
	 * @return 是否兼容的标识
	 */

	protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
		return (!(existingDefinition instanceof ScannedGenericBeanDefinition) ||  // explicitly registered overriding bean
				(newDefinition.getSource() != null && newDefinition.getSource().equals(existingDefinition.getSource())) ||  // scanned same file twice
				newDefinition.equals(existingDefinition));  // scanned equivalent class twice
	}


	/**
	 * 如果可能，从给定的注册表获取环境，否则返回一个新的标准环境。
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable) {
			return ((EnvironmentCapable) registry).getEnvironment();
		}
		return new StandardEnvironment();
	}

}
