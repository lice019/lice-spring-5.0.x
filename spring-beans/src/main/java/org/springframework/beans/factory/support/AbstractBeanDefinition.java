
package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


/**
 * AbstractBeanDefinition：封装了bean标签和bean注解的大部分属性
 * AbstractBeanDefinition:是一个抽象类，是把部分bean定义类的父类
 * AbstractBeanDefinition和GenericBeanDefinition：装配了<bean></bean>或注解@Compenent声明的bean实例的所有信息。实际上就是<bean></bean>标签的一个信息类，也是spring-IOC最重要的类。
 * AbstractBeanDefinition实现了BeanDefinition操作bean的接口
 * <p>
 * XML中bean的保存信息GenericBeanDefinition，大多数属性都是保存在父类中AbstractBeanDefinition
 * <p>
 * <p>
 * AbstractBeanDefinition：子类有RootBeanDefinition、ChildBeanDefinition、GenericBeanDefinition
 * ScannedGenericBeanDefinition、AnnotatedGenericBeanDefinition
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
		implements BeanDefinition, Cloneable {


	//默认bean的作用范围，对应bean属性的scope
	public static final String SCOPE_DEFAULT = "";


	//自动装配BeanFactory能力
	public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;


	//常量，该常量按名称指示自动装配bean属性。
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;


	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;


	//常量，该常量按类型指示自动装配bean属性。
	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;


	//常量，指示通过bean类的内省确定适当的自动装配策略。
	@Deprecated
	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;


	//常量，表示根本没有依赖项检查。
	public static final int DEPENDENCY_CHECK_NONE = 0;


	public static final int DEPENDENCY_CHECK_OBJECTS = 1;


	//常量，指示对“简单”属性的依赖项检查。
	public static final int DEPENDENCY_CHECK_SIMPLE = 2;


	//常量，指示对所有属性(对象引用以及“简单”属性)的依赖项检查。
	public static final int DEPENDENCY_CHECK_ALL = 3;


	public static final String INFER_METHOD = "(inferred)";


	//bean对象转成Object类型，可为null
	@Nullable
	private volatile Object beanClass;

	//bean的作用范围(scope),可为null，为null使用默认singleton
	@Nullable
	private String scope = SCOPE_DEFAULT;

	//是否是抽象，对应bean属性的abstract
	private boolean abstractFlag = false;

	//是否延迟加载，对应bean属性lazy-init
	private boolean lazyInit = false;

	//自动注入模式，对应bean属性autowire
	private int autowireMode = AUTOWIRE_NO;

	//依赖检查，Spring 3.0后弃用这个属性
	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	//用来表示一个bean的实例化依靠另外一个bean先实例化，对应bean属性depend-on
	@Nullable
	private String[] dependsOn;

	//autowire-candidate属性设置为false，这样容器在查找自动装配对象时候，将不考虑该bean，也就是他不会被考虑作为其他bean自动装配的候选者，但是该bean本身还是可以使自动装配来注入其他bean的
	private boolean autowireCandidate = true;

	//自动装配时当出现多个bean候选者时，将作为首选，对应bean属性的primary
	private boolean primary = false;

	//用于记录Qualifier，对应子元素的qualifiers，当自动注入的时候有相同的类型的bean时候，可以用Qualifier来指定注入的bean
	private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();


	@Nullable
	private Supplier<?> instanceSupplier;

	//允许访问非公开的构造器和方法，程序可以设置
	private boolean nonPublicAccessAllowed = true;

	//是否以一种宽松的模式解析构造函数，默认为true
	private boolean lenientConstructorResolution = true;

	@Nullable
	private String factoryBeanName;

	@Nullable
	private String factoryMethodName;

	//记录构造函数注入属性  ，对应bean属性construct-arg
	@Nullable
	private ConstructorArgumentValues constructorArgumentValues;

	//普通属性集合
	@Nullable
	private MutablePropertyValues propertyValues;

	//方法重写的持有者，记录lookup-method，replace-method元素
	@Nullable
	private MethodOverrides methodOverrides;

	//初始化方法，对应bean属性init-method
	@Nullable
	private String initMethodName;

	//销毁方法。对应bean属性destroy-method
	@Nullable
	private String destroyMethodName;

	//是否执行init-method，程序设置
	private boolean enforceInitMethod = true;

	//是否执行destroy-method，程序设置
	private boolean enforceDestroyMethod = true;

	//是否是用户定义的而不是应用程序本身定义，创建AOP时候为true，程序设置
	private boolean synthetic = false;

	//定义这个bean的应用，APPLICATION：用户，INFARSTRUCTURE；
	private int role = BeanDefinition.ROLE_APPLICATION;

	//bean的描述信息
	@Nullable
	private String description;

	//这个bean定义的资源
	@Nullable
	private Resource resource;


	//构造方法
	protected AbstractBeanDefinition() {
		this(null, null);
	}


	//使用给定的构造函数参数值和属性值创建一个新的AbstractBeanDefinition。
	protected AbstractBeanDefinition(@Nullable ConstructorArgumentValues cargs, @Nullable MutablePropertyValues pvs) {
		this.constructorArgumentValues = cargs;
		this.propertyValues = pvs;
	}


	//构造器，设置bean的属性信息
	protected AbstractBeanDefinition(BeanDefinition original) {
		//设置bean的属性信息
		setParentName(original.getParentName());
		setBeanClassName(original.getBeanClassName());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setLazyInit(original.isLazyInit());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setRole(original.getRole());
		setSource(original.getSource());
		copyAttributesFrom(original);

		//如果original为AbstractBeanDefinition的实例对象或子类，实现类
		if (original instanceof AbstractBeanDefinition) {
			//直接赋给当前的AbstractBeanDefinition对象
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			//获取bean的Class类型
			if (originalAbd.hasBeanClass()) {
				//将bean的Class字节码存储在AbstractBeanDefinition类中
				setBeanClass(originalAbd.getBeanClass());
			}
			//获取bean的构造参数
			if (originalAbd.hasConstructorArgumentValues()) {
				setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			}
			//获取bean的普通属性
			if (originalAbd.hasPropertyValues()) {
				setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			}
			//获取bean重写的方法
			if (originalAbd.hasMethodOverrides()) {
				setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependencyCheck(originalAbd.getDependencyCheck());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			setPrimary(originalAbd.isPrimary());
			copyQualifiersFrom(originalAbd);
			setInstanceSupplier(originalAbd.getInstanceSupplier());
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
			setInitMethodName(originalAbd.getInitMethodName());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodName(originalAbd.getDestroyMethodName());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setSynthetic(originalAbd.isSynthetic());
			setResource(originalAbd.getResource());
		} else {
			setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			setResourceDescription(original.getResourceDescription());
		}
	}


	public void overrideFrom(BeanDefinition other) {
		if (StringUtils.hasLength(other.getBeanClassName())) {
			setBeanClassName(other.getBeanClassName());
		}
		if (StringUtils.hasLength(other.getScope())) {
			setScope(other.getScope());
		}
		setAbstract(other.isAbstract());
		setLazyInit(other.isLazyInit());
		if (StringUtils.hasLength(other.getFactoryBeanName())) {
			setFactoryBeanName(other.getFactoryBeanName());
		}
		if (StringUtils.hasLength(other.getFactoryMethodName())) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		setRole(other.getRole());
		setSource(other.getSource());
		copyAttributesFrom(other);

		if (other instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
			if (otherAbd.hasBeanClass()) {
				setBeanClass(otherAbd.getBeanClass());
			}
			if (otherAbd.hasConstructorArgumentValues()) {
				getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
			}
			if (otherAbd.hasPropertyValues()) {
				getPropertyValues().addPropertyValues(other.getPropertyValues());
			}
			if (otherAbd.hasMethodOverrides()) {
				getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
			}
			setAutowireMode(otherAbd.getAutowireMode());
			setDependencyCheck(otherAbd.getDependencyCheck());
			setDependsOn(otherAbd.getDependsOn());
			setAutowireCandidate(otherAbd.isAutowireCandidate());
			setPrimary(otherAbd.isPrimary());
			copyQualifiersFrom(otherAbd);
			setInstanceSupplier(otherAbd.getInstanceSupplier());
			setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
			setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
			if (otherAbd.getInitMethodName() != null) {
				setInitMethodName(otherAbd.getInitMethodName());
				setEnforceInitMethod(otherAbd.isEnforceInitMethod());
			}
			if (otherAbd.getDestroyMethodName() != null) {
				setDestroyMethodName(otherAbd.getDestroyMethodName());
				setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
			}
			setSynthetic(otherAbd.isSynthetic());
			setResource(otherAbd.getResource());
		} else {
			getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
			getPropertyValues().addPropertyValues(other.getPropertyValues());
			setResourceDescription(other.getResourceDescription());
		}
	}


	public void applyDefaults(BeanDefinitionDefaults defaults) {
		setLazyInit(defaults.isLazyInit());
		setAutowireMode(defaults.getAutowireMode());
		setDependencyCheck(defaults.getDependencyCheck());
		setInitMethodName(defaults.getInitMethodName());
		setEnforceInitMethod(false);
		setDestroyMethodName(defaults.getDestroyMethodName());
		setEnforceDestroyMethod(false);
	}


	@Override
	public void setBeanClassName(@Nullable String beanClassName) {
		this.beanClass = beanClassName;
	}


	//通过Class字节码，来获取类名称
	@Override
	@Nullable
	public String getBeanClassName() {
		Object beanClassObject = this.beanClass;
		if (beanClassObject instanceof Class) {
			return ((Class<?>) beanClassObject).getName();
		} else {
			return (String) beanClassObject;
		}
	}


	public void setBeanClass(@Nullable Class<?> beanClass) {
		this.beanClass = beanClass;
	}


	//获取bean的运行时Class类型
	public Class<?> getBeanClass() throws IllegalStateException {
		Object beanClassObject = this.beanClass;
		//为null，直接抛异常
		if (beanClassObject == null) {
			throw new IllegalStateException("No bean class specified on bean definition");
		}
		//beanClassObject不为Class类型，直接抛异常
		if (!(beanClassObject instanceof Class)) {
			throw new IllegalStateException(
					"Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
		}
		return (Class<?>) beanClassObject;
	}


	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}


	//确定包装bean的类，必要时从指定的类名解析它。在调用已解析的bean类时，还将从其名称重新加载指定的类。
	@Nullable
	public Class<?> resolveBeanClass(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
		String className = getBeanClassName();
		if (className == null) {
			return null;
		}
		Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
		this.beanClass = resolvedClass;
		return resolvedClass;
	}


	@Override
	public void setScope(@Nullable String scope) {
		this.scope = scope;
	}


	@Override
	@Nullable
	public String getScope() {
		return this.scope;
	}


	//bean是否单例
	@Override
	public boolean isSingleton() {
		return SCOPE_SINGLETON.equals(this.scope) || SCOPE_DEFAULT.equals(this.scope);
	}


	//bean是否为原型
	@Override
	public boolean isPrototype() {
		return SCOPE_PROTOTYPE.equals(this.scope);
	}


	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}


	@Override
	public boolean isAbstract() {
		return this.abstractFlag;
	}


	@Override
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}


	@Override
	public boolean isLazyInit() {
		return this.lazyInit;
	}


	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}


	public int getAutowireMode() {
		return this.autowireMode;
	}


	//解析@Autowire自动注入的属性
	public int getResolvedAutowireMode() {
		if (this.autowireMode == AUTOWIRE_AUTODETECT) {
			// Work out whether to apply setter autowiring or constructor autowiring.
			// If it has a no-arg constructor it's deemed to be setter autowiring,
			// otherwise we'll try constructor autowiring.
			Constructor<?>[] constructors = getBeanClass().getConstructors();
			for (Constructor<?> constructor : constructors) {
				if (constructor.getParameterCount() == 0) {
					return AUTOWIRE_BY_TYPE;
				}
			}
			return AUTOWIRE_CONSTRUCTOR;
		} else {
			return this.autowireMode;
		}
	}


	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}


	public int getDependencyCheck() {
		return this.dependencyCheck;
	}


	@Override
	public void setDependsOn(@Nullable String... dependsOn) {
		this.dependsOn = dependsOn;
	}


	@Override
	@Nullable
	public String[] getDependsOn() {
		return this.dependsOn;
	}


	@Override
	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}


	@Override
	public boolean isAutowireCandidate() {
		return this.autowireCandidate;
	}


	@Override
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}


	@Override
	public boolean isPrimary() {
		return this.primary;
	}


	public void addQualifier(AutowireCandidateQualifier qualifier) {
		this.qualifiers.put(qualifier.getTypeName(), qualifier);
	}


	public boolean hasQualifier(String typeName) {
		return this.qualifiers.containsKey(typeName);
	}


	@Nullable
	public AutowireCandidateQualifier getQualifier(String typeName) {
		return this.qualifiers.get(typeName);
	}


	public Set<AutowireCandidateQualifier> getQualifiers() {
		return new LinkedHashSet<>(this.qualifiers.values());
	}


	public void copyQualifiersFrom(AbstractBeanDefinition source) {
		Assert.notNull(source, "Source must not be null");
		this.qualifiers.putAll(source.qualifiers);
	}


	public void setInstanceSupplier(@Nullable Supplier<?> instanceSupplier) {
		this.instanceSupplier = instanceSupplier;
	}


	@Nullable
	public Supplier<?> getInstanceSupplier() {
		return this.instanceSupplier;
	}


	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}


	public boolean isNonPublicAccessAllowed() {
		return this.nonPublicAccessAllowed;
	}


	public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
		this.lenientConstructorResolution = lenientConstructorResolution;
	}

	public boolean isLenientConstructorResolution() {
		return this.lenientConstructorResolution;
	}

	@Override
	public void setFactoryBeanName(@Nullable String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}


	@Override
	@Nullable
	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}


	@Override
	public void setFactoryMethodName(@Nullable String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}


	@Override
	@Nullable
	public String getFactoryMethodName() {
		return this.factoryMethodName;
	}


	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues = constructorArgumentValues;
	}


	@Override
	public ConstructorArgumentValues getConstructorArgumentValues() {
		if (this.constructorArgumentValues == null) {
			this.constructorArgumentValues = new ConstructorArgumentValues();
		}
		return this.constructorArgumentValues;
	}


	@Override
	public boolean hasConstructorArgumentValues() {
		return (this.constructorArgumentValues != null && !this.constructorArgumentValues.isEmpty());
	}


	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = propertyValues;
	}


	@Override
	public MutablePropertyValues getPropertyValues() {
		if (this.propertyValues == null) {
			this.propertyValues = new MutablePropertyValues();
		}
		return this.propertyValues;
	}


	@Override
	public boolean hasPropertyValues() {
		return (this.propertyValues != null && !this.propertyValues.isEmpty());
	}


	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = methodOverrides;
	}


	public MethodOverrides getMethodOverrides() {
		if (this.methodOverrides == null) {
			this.methodOverrides = new MethodOverrides();
		}
		return this.methodOverrides;
	}


	public boolean hasMethodOverrides() {
		return (this.methodOverrides != null && !this.methodOverrides.isEmpty());
	}


	public void setInitMethodName(@Nullable String initMethodName) {
		this.initMethodName = initMethodName;
	}


	@Nullable
	public String getInitMethodName() {
		return this.initMethodName;
	}


	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}


	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}


	public void setDestroyMethodName(@Nullable String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}


	@Nullable
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}


	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}


	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
	}


	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}


	public boolean isSynthetic() {
		return this.synthetic;
	}


	public void setRole(int role) {
		this.role = role;
	}


	@Override
	public int getRole() {
		return this.role;
	}


	public void setDescription(@Nullable String description) {
		this.description = description;
	}


	@Override
	@Nullable
	public String getDescription() {
		return this.description;
	}


	public void setResource(@Nullable Resource resource) {
		this.resource = resource;
	}


	@Nullable
	public Resource getResource() {
		return this.resource;
	}


	public void setResourceDescription(@Nullable String resourceDescription) {
		this.resource = (resourceDescription != null ? new DescriptiveResource(resourceDescription) : null);
	}


	@Override
	@Nullable
	public String getResourceDescription() {
		return (this.resource != null ? this.resource.getDescription() : null);
	}


	public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
		this.resource = new BeanDefinitionResource(originatingBd);
	}


	//获取原始的bean，没被装饰的bean
	@Override
	@Nullable
	public BeanDefinition getOriginatingBeanDefinition() {
		return (this.resource instanceof BeanDefinitionResource ?
				((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
	}


	public void validate() throws BeanDefinitionValidationException {
		if (hasMethodOverrides() && getFactoryMethodName() != null) {
			throw new BeanDefinitionValidationException(
					"Cannot combine static factory method with method overrides: " +
							"the static factory method must create the instance");
		}

		if (hasBeanClass()) {
			prepareMethodOverrides();
		}
	}


	/*
	 *验证及准备覆盖的方法overrides，
	 * 实际是处理XML配置bean标签的lookup-method和replace-method属性，者两个属性存放在 BeanDefinition 中的methodOverrides 属性里
	 */
	public void prepareMethodOverrides() throws BeanDefinitionValidationException {
		// Check that lookup methods exists.
		if (hasMethodOverrides()) {
			//提取bean标签的方法
			Set<MethodOverride> overrides = getMethodOverrides().getOverrides();
			//同步
			synchronized (overrides) {
				//遍历所有方法
				for (MethodOverride mo : overrides) {
					//对每个方法进行单独处理
					prepareMethodOverride(mo);
				}
			}
		}
	}


	/*
	 *对每个lookup-method 和 replace-method进行单独处理
	 */
	protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {

		// 获取对应类中对应方法名的个数
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if (count == 0) {
			throw new BeanDefinitionValidationException(
					"Invalid method override: no method with name '" + mo.getMethodName() +
							"' on class [" + getBeanClassName() + "]");
		} else if (count == 1) {
			// Mark override as not overloaded, to avoid the overhead of arg type checking.
			// 标记MethodOverride暂未被覆盖，避免参数类型检查的开销。
			mo.setOverloaded(false);
		}
		/*
		 * 通过以上两个函数的代码你能体会到它所要实现的功能吗？ 之前反复提到过，
		 * 在Spring 配置中存在 lookup-method 和 replace-method 两个配置功能，
		 * 而这两个配置的加载其实就是将配置统一存放在 BeanDefinition中的methodOverrides属性里,
		 * 这两个功能实现原理其实是在 bean 实例化的时候如果检测到存在 methodOverrides 属性，
		 * 会动态地为当前bean生成代理并使用对应的拦截器为 bean 做增强处理，
		 */
	}


	@Override
	public Object clone() {
		return cloneBeanDefinition();
	}


	public abstract AbstractBeanDefinition cloneBeanDefinition();

	//other实例是否相等于该bean
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeanDefinition)) {
			return false;
		}

		AbstractBeanDefinition that = (AbstractBeanDefinition) other;

		if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
		if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
		if (this.abstractFlag != that.abstractFlag) return false;
		if (this.lazyInit != that.lazyInit) return false;

		if (this.autowireMode != that.autowireMode) return false;
		if (this.dependencyCheck != that.dependencyCheck) return false;
		if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
		if (this.autowireCandidate != that.autowireCandidate) return false;
		if (!ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers)) return false;
		if (this.primary != that.primary) return false;

		if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
		if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
		if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;
		if (!ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides)) return false;

		if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
		if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
		if (this.enforceInitMethod != that.enforceInitMethod) return false;
		if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
		if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

		if (this.synthetic != that.synthetic) return false;
		if (this.role != that.role) return false;

		return super.equals(other);
	}

	//bean的hash值设置
	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
		hashCode = 29 * hashCode + super.hashCode();
		return hashCode;
	}

	//toString方式输出bean的信息
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("class [");
		sb.append(getBeanClassName()).append("]");
		sb.append("; scope=").append(this.scope);
		sb.append("; abstract=").append(this.abstractFlag);
		sb.append("; lazyInit=").append(this.lazyInit);
		sb.append("; autowireMode=").append(this.autowireMode);
		sb.append("; dependencyCheck=").append(this.dependencyCheck);
		sb.append("; autowireCandidate=").append(this.autowireCandidate);
		sb.append("; primary=").append(this.primary);
		sb.append("; factoryBeanName=").append(this.factoryBeanName);
		sb.append("; factoryMethodName=").append(this.factoryMethodName);
		sb.append("; initMethodName=").append(this.initMethodName);
		sb.append("; destroyMethodName=").append(this.destroyMethodName);
		if (this.resource != null) {
			sb.append("; defined in ").append(this.resource.getDescription());
		}
		return sb.toString();
	}

}
