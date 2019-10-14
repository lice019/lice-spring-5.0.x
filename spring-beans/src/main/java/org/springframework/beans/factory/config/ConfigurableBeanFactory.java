package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * ConfigurableBeanFactory： 提 供 配 置 Factory 的 各 种 方 法。
 *
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 * @since 03.11.2003
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	//bean的scope的singleton单例
	String SCOPE_SINGLETON = "singleton";

	//bean的scope的prototype多例
	String SCOPE_PROTOTYPE = "prototype";


	//设置此bean工厂的父类。
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	//设置bean的类加载器
	void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

	//获取bean的类加载器
	@Nullable
	ClassLoader getBeanClassLoader();

	//指定用于类型匹配目的的临时类加载器。
	void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

	//获取临时类加载器
	@Nullable
	ClassLoader getTempClassLoader();

	//设置是否缓存bean元数据，如给定的bean定义(以合并方式)和已解析的bean类。默认的是。
	void setCacheBeanMetadata(boolean cacheBeanMetadata);

	//是否缓存bean的元数据(注解)
	boolean isCacheBeanMetadata();

	//指定bean定义值中的表达式的解析策略。
	void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

	@Nullable
	BeanExpressionResolver getBeanExpressionResolver();

	//指定用于转换属性值的Spring 3.0转换服务，作为javabean propertyeditor的替代方案。
	void setConversionService(@Nullable ConversionService conversionService);

	@Nullable
	ConversionService getConversionService();

	/**
	 * 添加要应用于所有bean创建过程的propertyeditorregistry。
	 * 这样的注册器创建新的PropertyEditor实例，并在给定的注册表上注册它们，每次尝试创建bean时都是新的。这避免了在定制编辑器上同步的需要;因此，通常更可取的方法是使用此方法，而不是使用{@link #registerCustomEditor}。
	 *
	 * @param registrar the PropertyEditorRegistrar to register
	 */
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	/**
	 * 属性的所有属性注册给定的自定义属性编辑器给定的类型。在工厂配置期间调用。
	 * 注意，这个方法将注册一个共享的自定义编辑器实例;
	 * 为了线程安全，对该实例的访问将被同步。它是通常更可取的方法是使用{@link #addPropertyEditorRegistrar}
	 * 此方法，以避免对自定义编辑器进行同步。
	 *
	 * @param requiredType        type of the property
	 * @param propertyEditorClass the {@link PropertyEditor} class to register
	 */
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	//使用在这个BeanFactory中注册的自定义编辑器初始化给定的PropertyEditorRegistry。
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	//设置一个自定义类型转换器，这个BeanFactory应该使用它来转换bean属性值、构造函数参数值等。
	void setTypeConverter(TypeConverter typeConverter);

	//获取此BeanFactory使用的类型转换器。这可能是新鲜的
	//实例，因为typeconverter通常不是线程安全的。
	//如果默认的PropertyEditor机制是活动的，返回的TypeConverter将知道所有已注册的自定义编辑器。
	TypeConverter getTypeConverter();

	/**
	 * 为嵌入的值(如注释属性)添加字符串解析器。
	 *
	 * @param valueResolver the String resolver to apply to embedded values
	 * @since 3.0
	 */
	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 * Determine whether an embedded value resolver has been registered with this
	 * bean factory, to be applied through {@link #resolveEmbeddedValue(String)}.
	 *
	 * @since 4.3
	 */
	boolean hasEmbeddedValueResolver();

	/**
	 * Resolve the given embedded value, e.g. an annotation attribute.
	 *
	 * @param value the value to resolve
	 * @return the resolved value (may be the original value as-is)
	 * @since 3.0
	 */
	@Nullable
	String resolveEmbeddedValue(String value);

	/**
	 * Add a new BeanPostProcessor that will get applied to beans created
	 * by this factory. To be invoked during factory configuration.
	 * <p>Note: Post-processors submitted here will be applied in the order of
	 * registration; any ordering semantics expressed through implementing the
	 * {@link org.springframework.core.Ordered} interface will be ignored. Note
	 * that autodetected post-processors (e.g. as beans in an ApplicationContext)
	 * will always be applied after programmatically registered ones.
	 *
	 * @param beanPostProcessor the post-processor to register
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

	/**
	 * Return the current number of registered BeanPostProcessors, if any.
	 */
	int getBeanPostProcessorCount();

	/**
	 * Register the given scope, backed by the given Scope implementation.
	 *
	 * @param scopeName the scope identifier
	 * @param scope     the backing Scope implementation
	 */
	void registerScope(String scopeName, Scope scope);

	/**
	 * Return the names of all currently registered scopes.
	 * <p>This will only return the names of explicitly registered scopes.
	 * Built-in scopes such as "singleton" and "prototype" won't be exposed.
	 *
	 * @return the array of scope names, or an empty array if none
	 * @see #registerScope
	 */
	String[] getRegisteredScopeNames();

	//返回给定范围名称的范围实现(如果有的话)。这将只返回显式注册的范围。内置范围，如“单例”和“原型”将不会公开。
	@Nullable
	Scope getRegisteredScope(String scopeName);

	//提供与此工厂相关的安全访问控制上下文。
	AccessControlContext getAccessControlContext();


	//从其他的工厂复制所有相关的配置信息
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	//别名注册器
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;

	//别名解析
	void resolveAliases(StringValueResolver valueResolver);

	//返回给定bean名称的合并bean定义，必要时将子bean定义与其父bean定义合并。
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	//确定具有给定名称的bean是否为FactoryBean。
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	//显式控制指定bean的当前创建状态。仅供容器内部使用。
	void setCurrentlyInCreation(String beanName, boolean inCreation);

	//确定指定的bean当前是否在创建中。
	boolean isCurrentlyInCreation(String beanName);

	//为给定bean注册一个依赖bean，在销毁给定bean之前销毁它。
	void registerDependentBean(String beanName, String dependentBeanName);

	//返回依赖于指定bean的所有bean的名称(如果有的话)。
	String[] getDependentBeans(String beanName);

	//返回指定bean所依赖的所有bean的名称(如果有的话)。
	String[] getDependenciesForBean(String beanName);

	//通过来bean的实例对象和bean的名称，来找到相应的bean，并将它销毁
	void destroyBean(String beanName, Object beanInstance);

	void destroyScopedBean(String beanName);

	void destroySingletons();

}
