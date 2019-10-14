
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * BeanDefinitionReaderUtils有两大功能：
 * （1）、将AbstractBeanDefinition和GenericBeanDefinition等bean定义对象注册到IOC
 * （2）、生成容器中bean实例的唯一标识，beanName
 * <p>
 * 主要供内部使用。
 *
 * @see PropertiesBeanDefinitionReader
 * @see org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader
 */
public class BeanDefinitionReaderUtils {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR = BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR;


	/*
	 *创建AbstractBeanDefinition类型的GenericBeanDefinition
	 */
	public static AbstractBeanDefinition createBeanDefinition(
			@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {

		//创建GenericBeanDefinition
		GenericBeanDefinition bd = new GenericBeanDefinition();
		// parentName 可能为空
		bd.setParentName(parentName);
		if (className != null) {
			//如果classLoader不为空，则使用以传入的classLoader同一虚拟机加载类对象，否则只是记录className
			if (classLoader != null) {
				bd.setBeanClass(ClassUtils.forName(className, classLoader));
			} else {
				bd.setBeanClassName(className);
			}
		}
		return bd;
	}

	/**
	 * Generate a bean name for the given top-level bean definition,
	 * unique within the given bean factory.
	 *
	 * @param beanDefinition the bean definition to generate a bean name for
	 * @param registry       the bean factory that the definition is going to be
	 *                       registered with (to check for existing bean names)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 *                                      for the given bean definition
	 * @see #generateBeanName(BeanDefinition, BeanDefinitionRegistry, boolean)
	 */
	public static String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		return generateBeanName(beanDefinition, registry, false);
	}

	/**
	 * Generate a bean name for the given bean definition, unique within the
	 * given bean factory.
	 *
	 * @param definition  the bean definition to generate a bean name for
	 * @param registry    the bean factory that the definition is going to be
	 *                    registered with (to check for existing bean names)
	 * @param isInnerBean whether the given bean definition will be registered
	 *                    as inner bean or as top-level bean (allowing for special name generation
	 *                    for inner beans versus top-level beans)
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 *                                      for the given bean definition
	 */
	public static String generateBeanName(
			BeanDefinition definition, BeanDefinitionRegistry registry, boolean isInnerBean)
			throws BeanDefinitionStoreException {

		String generatedBeanName = definition.getBeanClassName();
		if (generatedBeanName == null) {
			if (definition.getParentName() != null) {
				generatedBeanName = definition.getParentName() + "$child";
			} else if (definition.getFactoryBeanName() != null) {
				generatedBeanName = definition.getFactoryBeanName() + "$created";
			}
		}
		if (!StringUtils.hasText(generatedBeanName)) {
			throw new BeanDefinitionStoreException("Unnamed bean definition specifies neither " +
					"'class' nor 'parent' nor 'factory-bean' - can't generate bean name");
		}

		String id = generatedBeanName;
		if (isInnerBean) {
			// Inner bean: generate identity hashcode suffix.
			id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(definition);
		} else {
			// Top-level bean: use plain class name.
			// Increase counter until the id is unique.
			int counter = -1;
			while (counter == -1 || registry.containsBeanDefinition(id)) {
				counter++;
				id = generatedBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
			}
		}
		return id;
	}

	/*
	 * 将一个bean的定义对象(如：AbstractBeanDefinition等)对象注册到bean工厂中
	 * 解析的beanDefinition都会被注册到BeanDefinitionRegistry类型的实例registry中，而对于beanDefinition的注册分成了两部分：
	 * 1、通过beanName的注册BeanDefinition
	 * 2、通过别名注册BeanDefinition
	 */
	public static void registerBeanDefinition(
			BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		// Register bean definition under primary name.
		//　使用beanName做唯一标识注册
		String beanName = definitionHolder.getBeanName();
		/*
		 *通过BeanDefinitionRegistry来注册bean到工厂中
		 * key--beanName
		 * value--BeanDefinition的实现类或BeanDefinition的实现类的子类（AbstractBeanDefinition、GenericBeanDefinition等）
		 * definitionHolder.getBeanDefinition()：通过bean定义对象的持有者获取出bean定义对象
		 */
		registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

		// Register aliases for bean name, if any.
		//注册所有的别名
		String[] aliases = definitionHolder.getAliases();
		if (aliases != null) {
			for (String alias : aliases) {
				registry.registerAlias(beanName, alias);
			}
		}
	}

	/**
	 * Register the given bean definition with a generated name,
	 * unique within the given bean factory.
	 *
	 * @param definition the bean definition to generate a bean name for
	 * @param registry   the bean factory to register with
	 * @return the generated bean name
	 * @throws BeanDefinitionStoreException if no unique name can be generated
	 *                                      for the given bean definition or the definition cannot be registered
	 */
	public static String registerWithGeneratedName(
			AbstractBeanDefinition definition, BeanDefinitionRegistry registry)
			throws BeanDefinitionStoreException {

		String generatedName = generateBeanName(definition, registry, false);
		registry.registerBeanDefinition(generatedName, definition);
		return generatedName;
	}

}
