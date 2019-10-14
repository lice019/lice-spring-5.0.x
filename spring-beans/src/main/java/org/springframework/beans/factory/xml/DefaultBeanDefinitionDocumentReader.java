package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * DefaultBeanDefinitionDocumentReader：该类时spring的读取XML配置的类，然后委托给BeanDefinitionParseDelegate类去解析XML配置。
 * BeanDefinitionParseDelegate会将XML配置加载成Document，通过解析Document来解析<bean></bean>标签，
 * 然后将解析到的bean属性封装到AbstractBeanDefinition和其子类GenericBeanDefinition中
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	//beans标签<beans></beans>的Element标识
	public static final String NESTED_BEANS_ELEMENT = "beans";

	//alias别名标签<alias></alias>的Element标识
	public static final String ALIAS_ELEMENT = "alias";

	//name的属性
	public static final String NAME_ATTRIBUTE = "name";

	//别名属性
	public static final String ALIAS_ATTRIBUTE = "alias";

	//import标签<import></import>的Element标识
	public static final String IMPORT_ELEMENT = "import";

	//资源属性
	public static final String RESOURCE_ATTRIBUTE = "resource";

	//配置文件属性
	public static final String PROFILE_ATTRIBUTE = "profile";


	protected final Log logger = LogFactory.getLog(getClass());

	//XML的context读取器
	@Nullable
	private XmlReaderContext readerContext;

	//bean定义的委托对象
	@Nullable
	private BeanDefinitionParserDelegate delegate;


	/**
	 * 该实现根据“spring-beans”XSD(或DTD)解析bean定义。
	 * 打开一个DOM文档;然后初始化在{@code }级别指定的默认设置;然后解析所包含的bean定义。
	 */
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		logger.debug("Loading bean definitions");
		//获取document中根节点，<beans></beans>
		Element root = doc.getDocumentElement();
		//由跟路径开始解析，注册<beans></beans>下的所有<bean>中的bean对象实例
		doRegisterBeanDefinitions(root);
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final XmlReaderContext getReaderContext() {
		Assert.state(this.readerContext != null, "No XmlReaderContext available");
		return this.readerContext;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
	 * to pull the source metadata from the supplied {@link Element}.
	 */
	@Nullable
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}


	/*
	 * 在给定的根{@code  <beans/>}元素中注册每个bean定义。
	 */
	protected void doRegisterBeanDefinitions(Element root) {
		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.
		//======翻译：
		//任何嵌套的元素都会在这个方法中引起递归。
		//为了正确地传播和保存 default-*属性，
		//跟踪当前(父)委托，它可能为空。创建新的(子)委托，其中包含对父委托的引用，用于备份，然后最终将this.delegate重置回其原始(父)引用。
		//这种行为模拟了一堆委托，实际上并不需要委托。

		//创建一个委托对象，作为第一个委托对象
		/*
		 * 为什么使用委托，因为<bean></bean>标签中也会含有其他的节点标签，如：<property ></property>等。
		 * 所以使用层级关系，逐级解析
		 */
		BeanDefinitionParserDelegate parent = this.delegate;
		//创建第二个委托对象BeanDefinitionParserDelegate
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {
			//由配置文件获取跟节点<beans></beans>的属性，就是DTD的约束
			/*
			 *在注册Bean的最开始是对PROFILE_ATTRIBUTE属性的解析，PROFILE_ATTRIBUTE是bean标签中profile属性
			 * < beans profile =" dev" >
			 *首先程序会获取beans节点是否定义了profile属性，如果定义了则会需要到环境变量中去寻找，
			 * 所以这里首先断言environment不可能为空，因为profile是可以同时指定多个的，需要程序对其拆分，
			 * 并解析每个profile是都符合环境变量中所定义的，不定义则不会浪费性能去解析。
			 */
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isInfoEnabled()) {
						logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		//解析<beans></beans>的前置处理
		preProcessXml(root);
		//由<beans></beans>开始解析<bean><bean>装配成bean实例对象存储到IOC中
		parseBeanDefinitions(root, this.delegate);
		//解析<beans></beans>的后置处理
		postProcessXml(root);
		//将委托对象改为原来第一个委托对象
		this.delegate = parent;
	}

	//创建委托者对象
	protected BeanDefinitionParserDelegate createDelegate(
			XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/*
	 * 解析文档根级别的元素:"import", "alias", "bean".
	 *
	 * @param root 文档的DOM根元素<beans></beans>
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			//获取<beans></beans>的所有子节点
			NodeList nl = root.getChildNodes();
			//遍历所有的子节点
			for (int i = 0; i < nl.getLength(); i++) {
				//子节点对象Node
				Node node = nl.item(i);
				//判断是否为Element的实例对象
				if (node instanceof Element) {
					//是Element的实例对象，就转成Element类型
					Element ele = (Element) node;
					/*
					 *在Spring的XML配置里面有两大类Bean声明，一个是默认的，
					 * 如：< bean id =" test" class =" test.TestBean"/ >
					 *另一类就是自定义的， 如：< tx:annotation-driven/ >
					 *两种方式的读取和解析的差异很大
					 */
					//默认标签的解析（使用spring提供的标签）
					if (delegate.isDefaultNamespace(ele)) {
						parseDefaultElement(ele, delegate);
					} else {
						//用户定义的标签（程序员自定义的标签）
						delegate.parseCustomElement(ele);
					}
				}
			}
		} else {
			//delegate.parseCustomElement(root)会返回一个BeanDefinition实例
			delegate.parseCustomElement(root);
		}
	}

	//Spring解析默认定义的标签（也就spring自定的bean）
	/*
	 *函数中的功能逻辑一目了然，分别对4种不同标签（ import、 alias、 bean 和 beans）做了不同的处理。
	 */
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		//以下会委托BeanDefinitionParserDelegate去解析Document，解析出的Document后，再做相应的处理
		//判断是为<import>的element
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			//将import的element对象交给importBeanDefinitionResource处理并配置成bean，在注册到IOC
			importBeanDefinitionResource(ele);
		} else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			//对 alias 标 签 的 处 理
			processAliasRegistration(ele);
		} else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			// 对 bean 标 签 的 处 理
			processBeanDefinition(ele, delegate);
		} else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// 对 beans 标 签 的 处 理
			doRegisterBeanDefinitions(ele);
		}
	}

	/*
	 * import标签的解析
	 * <beans>
	 *   <import resource="customerContext.xml"/>
	 *   <import resource="systemContext.xml"/>
	 * 	 ... ...
	 * </beans>
	 *
	 * 解析步骤：
	 * （1） 获取resource属性所表示的路径。
	 * （2） 解析路径中的系统属性，格式如“ ${ user.dir}”。
	 * （3） 判定location 是绝对路径还是相对路径。
	 * （4） 如果是绝对路径则递归调用bean的解析过程，进行另一次的解析。
	 * （5） 如果是相对路径则计算出绝对路径并进行解析。
	 * （6） 通知监听器，解析完成。
	 */
	protected void importBeanDefinitionResource(Element ele) {
		//获取<import resource>的resource属性中的值（即导入的文件路径）
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		//如果不存在resource属性，则不作任何处理
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// Resolve system properties: e.g. "${user.dir}"
		//解析系统属性:" $ {user.dir} "
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		//set容器
		Set<Resource> actualResources = new LinkedHashSet<>(4);

		// Discover whether the location is an absolute or relative URI
		//　判定location是决定 URI 还是相对 URI
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		} catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
		}

		// Absolute or relative?
		//　如果是绝对URI则直接根据地址加载对应的配置文件
		if (absoluteLocation) {
			try {
				//获取IOC中<import>标签的bean数量
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		} else {
			// No URL -> considering resource location as relative to the current file.
			try {
				//如果是相对地址则根据相对地址计算出绝对地址
				int importCount;
				//Resource存在多个子实现类，如VfsResource、 FileSystemResource等， 　 　 　
				// 而每个resource的createRelative 方式实现都不一样，所以这里先使用子类的方法尝试解析
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				} else {
					// 如果解析不成功，则使用默认的解析器ResourcePatternResolver进行解析
					String baseLocation = getReaderContext().getResource().getURL().toString();
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			} catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]",
						ele, ex);
			}
		}
		// 解析后进行监听器激活处理
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	/*
	 * 处理bean的Alias属性的注册
	 */
	protected void processAliasRegistration(Element ele) {
		//获取beanName
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		//获取alias
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {

				//注册alias
				getReaderContext().getRegistry().registerAlias(name, alias);
			} catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			// 别名注册后通知监听器做相应处理
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/*
	 * 处理解析XML配置中bean标签的bean定义
	 */
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		/*
		 * (1)、首先委托BeanDefinitionDelegate类的parseBeanDefinitionElement方法进行元素解析，
		 * 返回BeanDefinitionHolder类型的实例bdHolder，经过这个方法后，bdHolder实例已经包含XML配置文件中配置的各种属性的AbstractBeanDefinition对象实例，
		 * 例如 class、 name、 id、 alias 之类的属性。
		 */
		//BeanDefinitionHolder是一个bean定义对象(AbstractBeanDefinition等)的持有者，内部有一个BeanDefinition属性，用于获取相应的子类和实现类
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		/*
		 *（2） 当返回的bdHolder不为空的情况下若存在默认标签的子节点下再有自定义属性，还需要再次对自定义标签进行解析。
		 */
		if (bdHolder != null) {
			//当Spring中的bean使用的是默认的标签配置， 但是其中的子元素却使用了自定义的配置时，这句代码便会起作用了。
			/*
			 *例如：
			 * <bean id="test" class="test.MyClass">
			 *	<mybean:user username=" aaa"/ >
			 * </ bean >
			 */
			//主要是处理开发者自定的标签
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				/*
				 *（3） 解析完成后，需要对解析后的bdHolder进行注册，同样，注册操作委托给了BeanDefinitionReaderUtils的registerBeanDefinition方法。
				 */
				//
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			} catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			/*
			 *（4）最后发出响应事件，通知想关的监听器，这个bean已经加载完成了。
			 */
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}


	//给继承该类的类去实现加载XML配置文件的前置处理方法
	protected void preProcessXml(Element root) {
	}


	//给继承该类的类去实现加载XML配置文件的处理方法
	protected void postProcessXml(Element root) {
	}

}
