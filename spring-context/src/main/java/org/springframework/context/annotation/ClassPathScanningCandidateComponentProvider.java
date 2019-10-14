package org.springframework.context.annotation;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;


/**
 * ClassPathScanningCandidateComponentProvider:用于扫描并注册Bean,其默认只支持@Component注解。
 * 但@Component包含了@Service、@Repository、@Controller等。
 * <p>
 * 主要功能函数：
 * addIncludeFilter – 添加一个包含过滤器
 * addExcludeFilter – 添加一个排除过滤器
 * findCandidateComponents – 核心任务 : 从指定的某个包内扫描目标bean组件定义
 * resetFilters – 设置包含过滤器和排除过滤器为空
 * clearCache – 清除扫描过程中所记录的类的元数据信息缓存
 * <p>
 * 两类过滤器
 * 包含过滤器 – 符合条件的bean组件才会被作为候选
 * 排斥过滤器 – 符合条件的bean组件不会进入候选名单
 */
public class ClassPathScanningCandidateComponentProvider implements EnvironmentCapable, ResourceLoaderAware {

	//默认资源为.class文件
	static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";


	protected final Log logger = LogFactory.getLog(getClass());

	//resourcePattern="**/*.class"
	private String resourcePattern = DEFAULT_RESOURCE_PATTERN;

	//包含过滤器
	private final List<TypeFilter> includeFilters = new LinkedList<>();
	//排除过滤器
	private final List<TypeFilter> excludeFilters = new LinkedList<>();

	//环境类
	@Nullable
	private Environment environment;

	@Nullable
	private ConditionEvaluator conditionEvaluator;

	//资源解析器，资源路径解析器，能从指定的包路径，根据指定的pattern，加载相应的Resource
	@Nullable
	private ResourcePatternResolver resourcePatternResolver;

	//元数据读取器工厂，工厂类，用于生成读取类元数据的 MetadataReader
	@Nullable
	private MetadataReaderFactory metadataReaderFactory;


	@Nullable
	private CandidateComponentsIndex componentsIndex;


	//构造器
	protected ClassPathScanningCandidateComponentProvider() {
	}


	//创建类路径扫描候选组件Component提供器，是否使用默认过滤器
	//是否为{@link Component @Component}、{@link Repository @Repository}、{@link Service @Service}和{@link Controller @Controller}构造型注解注册默认过滤器
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters) {
		//构造方法里面传入了true之后，对象在它自己的includeFilters属性里面添加了一个AnnotationTypeFilter对象，并且此对象的参数是Component annotation
		this(useDefaultFilters, new StandardEnvironment());
	}


	//通过ClassPathScanningCandidateComponentProvider(true)的构造器，就可以扫描出任何出任何我们添加了@Component annotation的类
	public ClassPathScanningCandidateComponentProvider(boolean useDefaultFilters, Environment environment) {
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		//设置运行环境类，可以Environment的任意实现类或子孙类
		setEnvironment(environment);
		setResourceLoader(null);
	}


	public void setResourcePattern(String resourcePattern) {
		Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
		this.resourcePattern = resourcePattern;
	}

	//添加一个包含的过滤器，添加符合bean条件的组件
	public void addIncludeFilter(TypeFilter includeFilter) {
		this.includeFilters.add(includeFilter);
	}

	//添加一个排除过滤器，排除符合bean条件的组件
	public void addExcludeFilter(TypeFilter excludeFilter) {
		this.excludeFilters.add(0, excludeFilter);
	}

	//重置过滤器
	public void resetFilters(boolean useDefaultFilters) {
		this.includeFilters.clear();
		this.excludeFilters.clear();
		if (useDefaultFilters) {
			registerDefaultFilters();
		}
	}


	//注册器默认过滤器，符合bean条件的才可以注册
	@SuppressWarnings("unchecked")
	protected void registerDefaultFilters() {
		//添加一个过滤规则，使用了@Component,才能被注册
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
		//获取一个ClassPathScanningCandidateComponentProvider的类加载器ClassLoader
		ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.annotation.ManagedBean", cl)), false));
			logger.debug("JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning");
		} catch (ClassNotFoundException ex) {
			// JSR-250 1.1 API (as included in Java EE 6) not available - simply skip.
		}
		try {
			this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Named", cl)), false));
			logger.debug("JSR-330 'javax.inject.Named' annotation found and supported for component scanning");
		} catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}


	public void setEnvironment(Environment environment) {
		Assert.notNull(environment, "Environment must not be null");
		this.environment = environment;
		this.conditionEvaluator = null;
	}

	@Override
	public final Environment getEnvironment() {
		if (this.environment == null) {
			this.environment = new StandardEnvironment();
		}
		return this.environment;
	}

	//获取一个Bean定义的注册器
	@Nullable
	protected BeanDefinitionRegistry getRegistry() {
		return null;
	}


	@Override
	public void setResourceLoader(@Nullable ResourceLoader resourceLoader) {
		this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
		this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
		this.componentsIndex = CandidateComponentsIndexLoader.loadIndex(this.resourcePatternResolver.getClassLoader());
	}

	//资源加载器
	public final ResourceLoader getResourceLoader() {
		return getResourcePatternResolver();
	}

	private ResourcePatternResolver getResourcePatternResolver() {
		if (this.resourcePatternResolver == null) {
			this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
		}
		return this.resourcePatternResolver;
	}


	//设置一个元数据读取器工厂
	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

	//获取一个元数据读取器工厂
	public final MetadataReaderFactory getMetadataReaderFactory() {
		if (this.metadataReaderFactory == null) {
			this.metadataReaderFactory = new CachingMetadataReaderFactory();
		}
		return this.metadataReaderFactory;
	}


	//进行包扫描，找到候选的组件
	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		if (this.componentsIndex != null && indexSupportsIncludeFilters()) {
			return addCandidateComponentsFromIndex(this.componentsIndex, basePackage);
		} else {
			// 从指定的包内扫描目标组件bean
			return scanCandidateComponents(basePackage);
		}
	}


	private boolean indexSupportsIncludeFilters() {
		for (TypeFilter includeFilter : this.includeFilters) {
			if (!indexSupportsIncludeFilter(includeFilter)) {
				return false;
			}
		}
		return true;
	}

	//确定索引是否支持指定的include {@link TypeFilter}。
	private boolean indexSupportsIncludeFilter(TypeFilter filter) {
		if (filter instanceof AnnotationTypeFilter) {
			Class<? extends Annotation> annotation = ((AnnotationTypeFilter) filter).getAnnotationType();
			return (AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, annotation) ||
					annotation.getName().startsWith("javax."));
		}
		if (filter instanceof AssignableTypeFilter) {
			Class<?> target = ((AssignableTypeFilter) filter).getTargetType();
			return AnnotationUtils.isAnnotationDeclaredLocally(Indexed.class, target);
		}
		return false;
	}

	//提取原型以用于指定的兼容过滤器。
	@Nullable
	private String extractStereotype(TypeFilter filter) {
		if (filter instanceof AnnotationTypeFilter) {
			return ((AnnotationTypeFilter) filter).getAnnotationType().getName();
		}
		if (filter instanceof AssignableTypeFilter) {
			return ((AssignableTypeFilter) filter).getTargetType().getName();
		}
		return null;
	}

	//
	private Set<BeanDefinition> addCandidateComponentsFromIndex(CandidateComponentsIndex index, String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			Set<String> types = new HashSet<>();
			for (TypeFilter filter : this.includeFilters) {
				String stereotype = extractStereotype(filter);
				if (stereotype == null) {
					throw new IllegalArgumentException("Failed to extract stereotype from " + filter);
				}
				types.addAll(index.getCandidateTypes(basePackage, stereotype));
			}
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			for (String type : types) {
				//MetadataReader元数据的读取器
				MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(type);
				if (isCandidateComponent(metadataReader)) {
					AnnotatedGenericBeanDefinition sbd = new AnnotatedGenericBeanDefinition(
							metadataReader.getAnnotationMetadata());
					if (isCandidateComponent(sbd)) {
						if (debugEnabled) {
							logger.debug("Using candidate component class from index: " + type);
						}
						candidates.add(sbd);
					} else {
						if (debugEnabled) {
							logger.debug("Ignored because not a concrete top-level class: " + type);
						}
					}
				} else {
					if (traceEnabled) {
						logger.trace("Ignored because matching an exclude filter: " + type);
					}
				}
			}
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}

	//从指定的包内扫描目标组件bean
	private Set<BeanDefinition> scanCandidateComponents(String basePackage) {
		//设置容器，存储符合候选组件的bean
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
			//根据包名找到扫描的文件路径
			//也就就是将com.lice.bean转成com/lice/bean
			// 缺省情况下，这里只关注所有的java类文件，也就是.class结尾的字节码文件
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + '/' + this.resourcePattern;
			//文件格式的路径，获取Resource数组
			// 获取匹配 packageSearchPath pattern 的资源
			Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
			boolean traceEnabled = logger.isTraceEnabled();
			boolean debugEnabled = logger.isDebugEnabled();
			// 遍历每个找到的类对应的Resource 对象，获取其 MetadataReader
			// 判断这是否是一个符合包含过滤器，并且不在排斥过滤器内的bean组件定义类
			for (Resource resource : resources) {
				if (traceEnabled) {
					logger.trace("Scanning " + resource);
				}
				if (resource.isReadable()) {
					try {
						MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
						// 判断这是否是一个符合候选条件的bean组件定义类
						// 如果符合条件的话，将其添加到候选类集合 : candidates

						// 第一次判断 : 判断这是否是一个符合包含过滤器，并且不在排斥过滤器内的bean组件定义类
						if (isCandidateComponent(metadataReader)) {
							ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
							sbd.setResource(resource);
							sbd.setSource(resource);
							// 第二次判断 : 构造出BD之后再次检测是否符合候选条件
							// 1. 独立类 + 具体实现类 或者
							// 2. 独立类 + 抽象类 + 带有使用注解 Lookup 的方法
							if (isCandidateComponent(sbd)) {
								if (debugEnabled) {
									logger.debug("Identified candidate component class: " + resource);
								}
								candidates.add(sbd);
							} else {
								if (debugEnabled) {
									logger.debug("Ignored because not a concrete top-level class: " + resource);
								}
							}
						} else {
							if (traceEnabled) {
								logger.trace("Ignored because not matching any filter: " + resource);
							}
						}
					} catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to read candidate component class: " + resource, ex);
					}
				} else {
					if (traceEnabled) {
						logger.trace("Ignored because not readable: " + resource);
					}
				}
			}
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}


	//解析包
	protected String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(getEnvironment().resolveRequiredPlaceholders(basePackage));
	}

	//判断是为候选的component
	protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
		for (TypeFilter tf : this.excludeFilters) {
			if (tf.match(metadataReader, getMetadataReaderFactory())) {
				return false;
			}
		}
		for (TypeFilter tf : this.includeFilters) {
			if (tf.match(metadataReader, getMetadataReaderFactory())) {
				return isConditionMatch(metadataReader);
			}
		}
		return false;
	}

	//确定给定的类是否是基于任何{@code @Conditional}注释的候选组件。
	private boolean isConditionMatch(MetadataReader metadataReader) {
		if (this.conditionEvaluator == null) {
			this.conditionEvaluator =
					new ConditionEvaluator(getRegistry(), this.environment, this.resourcePatternResolver);
		}
		return !this.conditionEvaluator.shouldSkip(metadataReader.getAnnotationMetadata());
	}

	//确定给定的bean定义是否符合候选定义。
	//默认实现检查类是否不是接口，是否依赖于封闭的类。
	//可以在子类中重写。
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		AnnotationMetadata metadata = beanDefinition.getMetadata();
		return (metadata.isIndependent() && (metadata.isConcrete() ||
				(metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))));
	}


	//清除本地元数据的缓存
	public void clearCache() {
		if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
			// Clear cache in externally provided MetadataReaderFactory; this is a no-op
			// for a shared cache since it'll be cleared by the ApplicationContext.
			((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
		}
	}

}
