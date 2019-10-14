package org.springframework.context.annotation;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Extension of the {@link org.springframework.beans.factory.support.GenericBeanDefinition}
 * class, based on an ASM ClassReader, with support for annotation metadata exposed
 * through the {@link AnnotatedBeanDefinition} interface.
 *扩展{@link org.springframework.beans.factory.support。类，基于ASM类阅读器，支持通过{@link AnnotatedBeanDefinition}接口公开的注释元数据。
 *
 *
 * <p>This class does <i>not</i> load the bean {@code Class} early.
 * It rather retrieves all relevant metadata from the ".class" file itself,
 * parsed with the ASM ClassReader. It is functionally equivalent to
 * {@link AnnotatedGenericBeanDefinition#AnnotatedGenericBeanDefinition(AnnotationMetadata)}
 * but distinguishes by type beans that have been <em>scanned</em> vs those that have
 * been otherwise registered or detected by other means.
 *这个类执行而不是加载bean {@code class}。
 * 而是从“.class”检索所有相关元数据。类本身，用ASM类读取器解析。它在功能上等价于
 * {@link AnnotatedGenericBeanDefinition # AnnotatedGenericBeanDefinition (AnnotationMetadata)}
 * 但是通过扫描的类型bean与以其他方式注册或检测的类型bean的区别。
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see #getMetadata()
 * @see #getBeanClassName()
 * @see org.springframework.core.type.classreading.MetadataReaderFactory
 * @see AnnotatedGenericBeanDefinition
 */
@SuppressWarnings("serial")
public class ScannedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

	private final AnnotationMetadata metadata;


	/**
	 * Create a new ScannedGenericBeanDefinition for the class that the
	 * given MetadataReader describes.
	 * @param metadataReader the MetadataReader for the scanned target class
	 */
	public ScannedGenericBeanDefinition(MetadataReader metadataReader) {
		Assert.notNull(metadataReader, "MetadataReader must not be null");
		this.metadata = metadataReader.getAnnotationMetadata();
		setBeanClassName(this.metadata.getClassName());
	}


	@Override
	public final AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	@Nullable
	public MethodMetadata getFactoryMethodMetadata() {
		return null;
	}

}
