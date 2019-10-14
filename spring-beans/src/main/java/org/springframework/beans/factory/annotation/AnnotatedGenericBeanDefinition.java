package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Extension of the {@link org.springframework.beans.factory.support.GenericBeanDefinition}
 * class, adding support for annotation metadata exposed through the
 * {@link AnnotatedBeanDefinition} interface.
 *
 * <p>This GenericBeanDefinition variant is mainly useful for testing code that expects
 * to operate on an AnnotatedBeanDefinition, for example strategy implementations
 * in Spring's component scanning support (where the default definition class is
 * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition},
 * which also implements the AnnotatedBeanDefinition interface).
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @see AnnotatedBeanDefinition#getMetadata()
 * @see org.springframework.core.type.StandardAnnotationMetadata
 * @since 2.5
 */
@SuppressWarnings("serial")
public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

	//通过这个接口，可以得知类含有的注释
	private final AnnotationMetadata metadata;

	//存储工厂方法的元数据，这个接口的具体操作请看下文
	@Nullable
	private MethodMetadata factoryMethodMetadata;


	/**
	 * Create a new AnnotatedGenericBeanDefinition for the given bean class.
	 *
	 * @param beanClass the loaded bean class
	 */
	public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
		setBeanClass(beanClass);
		this.metadata = new StandardAnnotationMetadata(beanClass, true);
	}

	/**
	 * Create a new AnnotatedGenericBeanDefinition for the given annotation metadata,
	 * allowing for ASM-based processing and avoidance of early loading of the bean class.
	 * Note that this constructor is functionally equivalent to
	 * {@link org.springframework.context.annotation.ScannedGenericBeanDefinition
	 * ScannedGenericBeanDefinition}, however the semantics of the latter indicate that a
	 * bean was discovered specifically via component-scanning as opposed to other means.
	 *
	 * @param metadata the annotation metadata for the bean class in question
	 * @since 3.1.1
	 */
	public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
		Assert.notNull(metadata, "AnnotationMetadata must not be null");
		if (metadata instanceof StandardAnnotationMetadata) {
			setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
		} else {
			setBeanClassName(metadata.getClassName());
		}
		this.metadata = metadata;
	}

	/**
	 * Create a new AnnotatedGenericBeanDefinition for the given annotation metadata,
	 * based on an annotated class and a factory method on that class.
	 *
	 * @param metadata              the annotation metadata for the bean class in question
	 * @param factoryMethodMetadata metadata for the selected factory method
	 * @since 4.1.1
	 */
	public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata, MethodMetadata factoryMethodMetadata) {
		this(metadata);
		Assert.notNull(factoryMethodMetadata, "MethodMetadata must not be null");
		setFactoryMethodName(factoryMethodMetadata.getMethodName());
		this.factoryMethodMetadata = factoryMethodMetadata;
	}


	@Override
	public final AnnotationMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	@Nullable
	public final MethodMetadata getFactoryMethodMetadata() {
		return this.factoryMethodMetadata;
	}

}
