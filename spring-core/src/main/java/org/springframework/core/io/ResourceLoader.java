package org.springframework.core.io;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * 加载资源的策略接口(e..类路径或文件系统资源)。一个{@link org.springframework.context。需要ApplicationContext}来提供此功能，还需要扩展{@link org.springframework.core.io.support。ResourcePatternResolver}的支持。
 * {@link DefaultResourceLoader}是一个独立的实现，可以在ApplicationContext之外使用，也被{@link ResourceEditor}使用。
 * Bean属性类型资源和资源数组可以从字符串填充时，运行在ApplicationContext，使用特定上下文的资源加载策略。
 *
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:" */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;


	Resource getResource(String location);


	@Nullable
	ClassLoader getClassLoader();

}
