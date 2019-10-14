package org.springframework.beans.factory.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


/**
 * RootBeanDefinition保存的信息：
 * （1）、定义了id、别名与Bean的对应关系（BeanDefinitionHolder）
 * （2）、Bean的注解（AnnotatedElement）
 * （3）、具体的工厂方法（Class类型），包括工厂方法的返回类型，工厂方法的Method对象
 * （4）、构造函数、构造函数形参类型
 * （5）、Bean的class对象
 *
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	//BeanDefinitionHolder存储有Bean的名称、别名、BeanDefinition
	@Nullable
	private BeanDefinitionHolder decoratedDefinition;

	//是java反射包的接口，通过它可以查看Bean的注解信息
	@Nullable
	private AnnotatedElement qualifiedElement;

	//是否允许缓存
	boolean allowCaching = true;

	//工厂方法是否唯一
	boolean isFactoryMethodUnique = false;

	//封装了java.lang.reflect.Type,提供了泛型相关的操作
	@Nullable
	volatile ResolvableType targetType;

	/**
	 * Package-visible field for caching the determined Class of a given bean definition
	 */
	//包可见字段，用于缓存给定bean定义的已确定类
	@Nullable
	volatile Class<?> resolvedTargetType;

	/**
	 * Package-visible field for caching the return type of a generically typed factory method
	 */
	//用于缓存泛型工厂方法的返回类型的包可见字段
	@Nullable
	volatile ResolvableType factoryMethodReturnType;

	/**
	 * Common lock for the four constructor fields below
	 */
	//下面四个构造函数字段的公共锁
	final Object constructorArgumentLock = new Object();

	/**
	 * Package-visible field for caching the resolved constructor or factory method
	 */
	@Nullable
	Executable resolvedConstructorOrFactoryMethod;

	/**
	 * Package-visible field that marks the constructor arguments as resolved
	 */
	//包可见字段，该字段将构造函数参数标记为已解析
	boolean constructorArgumentsResolved = false;

	/**
	 * Package-visible field for caching fully resolved constructor arguments
	 */
	//包可见字段，用于缓存完全解析的构造函数参数
	@Nullable
	Object[] resolvedConstructorArguments;

	/**
	 * Package-visible field for caching partly prepared constructor arguments
	 */
	//包可见字段，用于缓存部分准备好的构造函数参数
	@Nullable
	Object[] preparedConstructorArguments;

	/**
	 * Common lock for the two post-processing fields below
	 */
	///下面两个后处理字段的公共锁
	final Object postProcessingLock = new Object();

	/**
	 * Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied
	 */
	//包可见字段，指示已应用了MergedBeanDefinitionPostProcessor
	boolean postProcessed = false;

	/**
	 * Package-visible field that indicates a before-instantiation post-processor having kicked in
	 */
	//包可见字段，指示实例化前后处理程序已启动
	@Nullable
	volatile Boolean beforeInstantiationResolved;

	@Nullable
	private Set<Member> externallyManagedConfigMembers;

	@Nullable
	private Set<String> externallyManagedInitMethods;

	@Nullable
	private Set<String> externallyManagedDestroyMethods;


	/**
	 * Create a new RootBeanDefinition, to be configured through its bean
	 * properties and configuration methods.
	 *
	 * @see #setBeanClass
	 * @see #setScope
	 * @see #setConstructorArgumentValues
	 * @see #setPropertyValues
	 */
	//构造方法
	public RootBeanDefinition() {
		super();
	}

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 *
	 * @param beanClass the class of the bean to instantiate
	 * @see #setBeanClass
	 */
	//为单例创建一个新的RootBeanDefinition。
	public RootBeanDefinition(@Nullable Class<?> beanClass) {
		super();
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton bean, constructing each instance
	 * through calling the given supplier (possibly a lambda or method reference).
	 *
	 * @param beanClass        the class of the bean to instantiate
	 * @param instanceSupplier the supplier to construct a bean instance,
	 *                         as an alternative to a declaratively specified factory method
	 * @see #setInstanceSupplier
	 * @since 5.0
	 */
	public <T> RootBeanDefinition(@Nullable Class<T> beanClass, @Nullable Supplier<T> instanceSupplier) {
		super();
		setBeanClass(beanClass);
		setInstanceSupplier(instanceSupplier);
	}

	/**
	 * Create a new RootBeanDefinition for a scoped bean, constructing each instance
	 * through calling the given supplier (possibly a lambda or method reference).
	 *
	 * @param beanClass        the class of the bean to instantiate
	 * @param scope            the name of the corresponding scope
	 * @param instanceSupplier the supplier to construct a bean instance,
	 *                         as an alternative to a declaratively specified factory method
	 * @see #setInstanceSupplier
	 * @since 5.0
	 */
	public <T> RootBeanDefinition(@Nullable Class<T> beanClass, String scope, @Nullable Supplier<T> instanceSupplier) {
		super();
		setBeanClass(beanClass);
		setScope(scope);
		setInstanceSupplier(instanceSupplier);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 *
	 * @param beanClass       the class of the bean to instantiate
	 * @param autowireMode    by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 *                        (not applicable to autowiring a constructor, thus ignored there)
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 *
	 * @param beanClass the class of the bean to instantiate
	 * @param cargs     the constructor argument values to apply
	 * @param pvs       the property values to apply
	 */
	public RootBeanDefinition(@Nullable Class<?> beanClass, @Nullable ConstructorArgumentValues cargs,
							  @Nullable MutablePropertyValues pvs) {

		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 *
	 * @param beanClassName the name of the class to instantiate
	 */
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 *
	 * @param beanClassName the name of the class to instantiate
	 * @param cargs         the constructor argument values to apply
	 * @param pvs           the property values to apply
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 *
	 * @param original the original bean definition to copy from
	 */
	//深克隆
	public RootBeanDefinition(RootBeanDefinition original) {
		super(original);
		//父类构造下，再添加RootBeanDefinition额外的属性
		this.decoratedDefinition = original.decoratedDefinition;
		this.qualifiedElement = original.qualifiedElement;
		this.allowCaching = original.allowCaching;
		this.isFactoryMethodUnique = original.isFactoryMethodUnique;
		this.targetType = original.targetType;
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 *
	 * @param original the original bean definition to copy from
	 */
	RootBeanDefinition(BeanDefinition original) {
		super(original);
	}


	@Override
	public String getParentName() {
		return null;
	}

	@Override
	public void setParentName(@Nullable String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}

	/**
	 * Register a target definition that is being decorated by this bean definition.
	 */
	public void setDecoratedDefinition(@Nullable BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	/**
	 * Return the target definition that is being decorated by this bean definition, if any.
	 */
	@Nullable
	public BeanDefinitionHolder getDecoratedDefinition() {
		return this.decoratedDefinition;
	}

	/**
	 * Specify the {@link AnnotatedElement} defining qualifiers,
	 * to be used instead of the target class or factory method.
	 *
	 * @see #setTargetType(ResolvableType)
	 * @see #getResolvedFactoryMethod()
	 * @since 4.3.3
	 */
	public void setQualifiedElement(@Nullable AnnotatedElement qualifiedElement) {
		this.qualifiedElement = qualifiedElement;
	}

	/**
	 * Return the {@link AnnotatedElement} defining qualifiers, if any.
	 * Otherwise, the factory method and target class will be checked.
	 *
	 * @since 4.3.3
	 */
	@Nullable
	public AnnotatedElement getQualifiedElement() {
		return this.qualifiedElement;
	}

	/**
	 * Specify a generics-containing target type of this bean definition, if known in advance.
	 *
	 * @since 4.3.3
	 */
	public void setTargetType(ResolvableType targetType) {
		this.targetType = targetType;
	}

	/**
	 * Specify the target type of this bean definition, if known in advance.
	 *
	 * @since 3.2.2
	 */
	public void setTargetType(@Nullable Class<?> targetType) {
		this.targetType = (targetType != null ? ResolvableType.forClass(targetType) : null);
	}

	/**
	 * Return the target type of this bean definition, if known
	 * (either specified in advance or resolved on first instantiation).
	 *
	 * @since 3.2.2
	 */
	@Nullable
	public Class<?> getTargetType() {
		if (this.resolvedTargetType != null) {
			return this.resolvedTargetType;
		}
		ResolvableType targetType = this.targetType;
		return (targetType != null ? targetType.resolve() : null);
	}

	/**
	 * Specify a factory method name that refers to a non-overloaded method.
	 */
	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	/**
	 * Check whether the given candidate qualifies as a factory method.
	 */
	public boolean isFactoryMethod(Method candidate) {
		return candidate.getName().equals(getFactoryMethodName());
	}

	/**
	 * Return the resolved factory method as a Java Method object, if available.
	 *
	 * @return the factory method, or {@code null} if not found or not resolved yet
	 */
	//以下的操作，进行线程同步
	@Nullable
	public Method getResolvedFactoryMethod() {
		synchronized (this.constructorArgumentLock) {
			Executable candidate = this.resolvedConstructorOrFactoryMethod;
			return (candidate instanceof Method ? (Method) candidate : null);
		}
	}

	public void registerExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedConfigMembers == null) {
				this.externallyManagedConfigMembers = new HashSet<>(1);
			}
			this.externallyManagedConfigMembers.add(configMember);
		}
	}

	public boolean isExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null &&
					this.externallyManagedConfigMembers.contains(configMember));
		}
	}

	public void registerExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedInitMethods == null) {
				this.externallyManagedInitMethods = new HashSet<>(1);
			}
			this.externallyManagedInitMethods.add(initMethod);
		}
	}

	public boolean isExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null &&
					this.externallyManagedInitMethods.contains(initMethod));
		}
	}

	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedDestroyMethods == null) {
				this.externallyManagedDestroyMethods = new HashSet<>(1);
			}
			this.externallyManagedDestroyMethods.add(destroyMethod);
		}
	}

	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null &&
					this.externallyManagedDestroyMethods.contains(destroyMethod));
		}
	}


	//克隆Bean
	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}

}
