package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Parser for the {@code <context:component-scan/>} element.
 * ComponentScanBeanDefinitionParser：解析<context:component-scan/>标签
 *
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.5
 */
public class ComponentScanBeanDefinitionParser implements BeanDefinitionParser {

	//包名
	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";

	//资源模式
	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resource-pattern";

	//过滤器
	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";

	//注解配置
	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";

	private static final String NAME_GENERATOR_ATTRIBUTE = "name-generator";

	//解析范围
	private static final String SCOPE_RESOLVER_ATTRIBUTE = "scope-resolver";


	private static final String SCOPED_PROXY_ATTRIBUTE = "scoped-proxy";

	//外置过滤器
	private static final String EXCLUDE_FILTER_ELEMENT = "exclude-filter";

	private static final String INCLUDE_FILTER_ELEMENT = "include-filter";

	private static final String FILTER_TYPE_ATTRIBUTE = "type";

	private static final String FILTER_EXPRESSION_ATTRIBUTE = "expression";


	/**
	 *
	 * @param element 要解析为一个或多个{@link BeanDefinition BeanDefinition}的元素
	 * @param parserContext 封装解析过程的当前状态的对象;
	 * provides access to a {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}
	 * @return
	 */
	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		//获取需要扫描的包名(例如：arg.apache.spring)
		String basePackage = element.getAttribute(BASE_PACKAGE_ATTRIBUTE);
		//解析包
		basePackage = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(basePackage);
		//字符转成数组（arg.apache.spring）--->[arg,apache,spring]
		String[] basePackages = StringUtils.tokenizeToStringArray(basePackage,
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

		// Actually scan for bean definitions and register them.--实际上，扫描bean定义并注册它们
		ClassPathBeanDefinitionScanner scanner = configureScanner(parserContext, element);
		Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan(basePackages);
		registerComponents(parserContext.getReaderContext(), beanDefinitions, element);

		return null;
	}

	protected ClassPathBeanDefinitionScanner configureScanner(ParserContext parserContext, Element element) {
		//使用默认过滤器
		boolean useDefaultFilters = true;
		//获取element的属性值
		if (element.hasAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)) {
			useDefaultFilters = Boolean.valueOf(element.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE));
		}

		// Delegate bean definition registration to scanner class.--将bean定义注册委托给扫描器类。
		ClassPathBeanDefinitionScanner scanner = createScanner(parserContext.getReaderContext(), useDefaultFilters);
		scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
		scanner.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());

		if (element.hasAttribute(RESOURCE_PATTERN_ATTRIBUTE)) {
			scanner.setResourcePattern(element.getAttribute(RESOURCE_PATTERN_ATTRIBUTE));
		}

		try {
			parseBeanNameGenerator(element, scanner);
		}
		catch (Exception ex) {
			parserContext.getReaderContext().error(ex.getMessage(), parserContext.extractSource(element), ex.getCause());
		}

		try {
			parseScope(element, scanner);
		}
		catch (Exception ex) {
			parserContext.getReaderContext().error(ex.getMessage(), parserContext.extractSource(element), ex.getCause());
		}

		parseTypeFilters(element, scanner, parserContext);

		return scanner;
	}

	protected ClassPathBeanDefinitionScanner createScanner(XmlReaderContext readerContext, boolean useDefaultFilters) {
		return new ClassPathBeanDefinitionScanner(readerContext.getRegistry(), useDefaultFilters,
				readerContext.getEnvironment(), readerContext.getResourceLoader());
	}

	protected void registerComponents(
			XmlReaderContext readerContext, Set<BeanDefinitionHolder> beanDefinitions, Element element) {

		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);

		for (BeanDefinitionHolder beanDefHolder : beanDefinitions) {
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
		}

		// Register annotation config processors, if necessary.
		boolean annotationConfig = true;
		if (element.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) {
			annotationConfig = Boolean.valueOf(element.getAttribute(ANNOTATION_CONFIG_ATTRIBUTE));
		}
		if (annotationConfig) {
			Set<BeanDefinitionHolder> processorDefinitions =
					AnnotationConfigUtils.registerAnnotationConfigProcessors(readerContext.getRegistry(), source);
			for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
				compositeDef.addNestedComponent(new BeanComponentDefinition(processorDefinition));
			}
		}

		readerContext.fireComponentRegistered(compositeDef);
	}

	protected void parseBeanNameGenerator(Element element, ClassPathBeanDefinitionScanner scanner) {
		if (element.hasAttribute(NAME_GENERATOR_ATTRIBUTE)) {
			BeanNameGenerator beanNameGenerator = (BeanNameGenerator) instantiateUserDefinedStrategy(
					element.getAttribute(NAME_GENERATOR_ATTRIBUTE), BeanNameGenerator.class,
					scanner.getResourceLoader().getClassLoader());
			scanner.setBeanNameGenerator(beanNameGenerator);
		}
	}

	protected void parseScope(Element element, ClassPathBeanDefinitionScanner scanner) {
		// Register ScopeMetadataResolver if class name provided.
		if (element.hasAttribute(SCOPE_RESOLVER_ATTRIBUTE)) {
			if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
				throw new IllegalArgumentException(
						"Cannot define both 'scope-resolver' and 'scoped-proxy' on <component-scan> tag");
			}
			ScopeMetadataResolver scopeMetadataResolver = (ScopeMetadataResolver) instantiateUserDefinedStrategy(
					element.getAttribute(SCOPE_RESOLVER_ATTRIBUTE), ScopeMetadataResolver.class,
					scanner.getResourceLoader().getClassLoader());
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
			String mode = element.getAttribute(SCOPED_PROXY_ATTRIBUTE);
			if ("targetClass".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);
			}
			else if ("interfaces".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.INTERFACES);
			}
			else if ("no".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.NO);
			}
			else {
				throw new IllegalArgumentException("scoped-proxy only supports 'no', 'interfaces' and 'targetClass'");
			}
		}
	}

	protected void parseTypeFilters(Element element, ClassPathBeanDefinitionScanner scanner, ParserContext parserContext) {
		// Parse exclude and include filter elements.
		ClassLoader classLoader = scanner.getResourceLoader().getClassLoader();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = parserContext.getDelegate().getLocalName(node);
				try {
					if (INCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter((Element) node, classLoader, parserContext);
						scanner.addIncludeFilter(typeFilter);
					}
					else if (EXCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter((Element) node, classLoader, parserContext);
						scanner.addExcludeFilter(typeFilter);
					}
				}
				catch (ClassNotFoundException ex) {
					parserContext.getReaderContext().warning(
							"Ignoring non-present type filter class: " + ex, parserContext.extractSource(element));
				}
				catch (Exception ex) {
					parserContext.getReaderContext().error(
							ex.getMessage(), parserContext.extractSource(element), ex.getCause());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected TypeFilter createTypeFilter(Element element, @Nullable ClassLoader classLoader,
			ParserContext parserContext) throws ClassNotFoundException {

		String filterType = element.getAttribute(FILTER_TYPE_ATTRIBUTE);
		String expression = element.getAttribute(FILTER_EXPRESSION_ATTRIBUTE);
		expression = parserContext.getReaderContext().getEnvironment().resolvePlaceholders(expression);
		if ("annotation".equals(filterType)) {
			return new AnnotationTypeFilter((Class<Annotation>) ClassUtils.forName(expression, classLoader));
		}
		else if ("assignable".equals(filterType)) {
			return new AssignableTypeFilter(ClassUtils.forName(expression, classLoader));
		}
		else if ("aspectj".equals(filterType)) {
			return new AspectJTypeFilter(expression, classLoader);
		}
		else if ("regex".equals(filterType)) {
			return new RegexPatternTypeFilter(Pattern.compile(expression));
		}
		else if ("custom".equals(filterType)) {
			Class<?> filterClass = ClassUtils.forName(expression, classLoader);
			if (!TypeFilter.class.isAssignableFrom(filterClass)) {
				throw new IllegalArgumentException(
						"Class is not assignable to [" + TypeFilter.class.getName() + "]: " + expression);
			}
			return (TypeFilter) BeanUtils.instantiateClass(filterClass);
		}
		else {
			throw new IllegalArgumentException("Unsupported filter type: " + filterType);
		}
	}

	@SuppressWarnings("unchecked")
	private Object instantiateUserDefinedStrategy(
			String className, Class<?> strategyType, @Nullable ClassLoader classLoader) {

		Object result;
		try {
			result = ReflectionUtils.accessibleConstructor(ClassUtils.forName(className, classLoader)).newInstance();
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Class [" + className + "] for strategy [" +
					strategyType.getName() + "] not found", ex);
		}
		catch (Throwable ex) {
			throw new IllegalArgumentException("Unable to instantiate class [" + className + "] for strategy [" +
					strategyType.getName() + "]: a zero-argument constructor is required", ex);
		}

		if (!strategyType.isAssignableFrom(result.getClass())) {
			throw new IllegalArgumentException("Provided class name must be an implementation of " + strategyType);
		}
		return result;
	}

}
