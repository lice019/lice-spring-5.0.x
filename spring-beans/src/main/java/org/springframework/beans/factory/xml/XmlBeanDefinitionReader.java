package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.NullSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Constants;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;

/**
 * XmlBeanDefinitionReader：是spring专门用于读取xml配置来加载bean的读取器，与AnnotatedBeanDefinitionReader功能一样，而AnnotatedBeanDefinitionReader是spring专门用于注解式bean的读取器。
 * XML配置文件的读取是 Spring 中重要的功能，因为Spring的大部分功能都是以配置作为切入点的，
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	//指示应禁用验证。
	public static final int VALIDATION_NONE = XmlValidationModeDetector.VALIDATION_NONE;

	//指示应自动检测验证模式。
	public static final int VALIDATION_AUTO = XmlValidationModeDetector.VALIDATION_AUTO;

	//指示应使用DTD验证。
	public static final int VALIDATION_DTD = XmlValidationModeDetector.VALIDATION_DTD;

	//指示应使用XSD验证。
	public static final int VALIDATION_XSD = XmlValidationModeDetector.VALIDATION_XSD;


	//类的常量实例,
	private static final Constants constants = new Constants(XmlBeanDefinitionReader.class);

	//校验模式
	private int validationMode = VALIDATION_AUTO;

	//名称空间意识到
	private boolean namespaceAware = false;

	//DefaultBeanDefinitionDocumentReader解析dom的<beans><import>等节点元素读取器的Class字节码，使用泛型的上界限定
	private Class<? extends BeanDefinitionDocumentReader> documentReaderClass =
			DefaultBeanDefinitionDocumentReader.class;

	//问题报告对象，实际是异常信息
	private ProblemReporter problemReporter = new FailFastProblemReporter();

	//读取器事件的监听器
	private ReaderEventListener eventListener = new EmptyReaderEventListener();

	//资源抽取器
	private SourceExtractor sourceExtractor = new NullSourceExtractor();

	//命名空间的处理器解析器
	@Nullable
	private NamespaceHandlerResolver namespaceHandlerResolver;

	//Document加载器
	//DocumentLoader： 定义从资源文件加载到转换为 Document 的功能。
	private DocumentLoader documentLoader = new DefaultDocumentLoader();

	//实体解析器
	@Nullable
	private EntityResolver entityResolver;

	//异常处理器
	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	//XML检验模式探测器
	private final XmlValidationModeDetector validationModeDetector = new XmlValidationModeDetector();

	//当前正在加载的资源的线程
	private final ThreadLocal<Set<EncodedResource>> resourcesCurrentlyBeingLoaded =
			new NamedThreadLocal<>("XML bean definition resources currently being loaded");


	//初始化一个XML配置bean的读取器，并将BeanDefinition注册中心传入
	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}


	//设置校验
	public void setValidating(boolean validating) {
		this.validationMode = (validating ? VALIDATION_AUTO : VALIDATION_NONE);
		this.namespaceAware = !validating;
	}

	public void setValidationModeName(String validationModeName) {
		setValidationMode(constants.asNumber(validationModeName).intValue());
	}

	public void setValidationMode(int validationMode) {
		this.validationMode = validationMode;
	}


	public int getValidationMode() {
		return this.validationMode;
	}


	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}


	public boolean isNamespaceAware() {
		return this.namespaceAware;
	}


	public void setProblemReporter(@Nullable ProblemReporter problemReporter) {
		this.problemReporter = (problemReporter != null ? problemReporter : new FailFastProblemReporter());
	}


	public void setEventListener(@Nullable ReaderEventListener eventListener) {
		this.eventListener = (eventListener != null ? eventListener : new EmptyReaderEventListener());
	}


	public void setSourceExtractor(@Nullable SourceExtractor sourceExtractor) {
		this.sourceExtractor = (sourceExtractor != null ? sourceExtractor : new NullSourceExtractor());
	}

	public void setNamespaceHandlerResolver(@Nullable NamespaceHandlerResolver namespaceHandlerResolver) {
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}


	public void setDocumentLoader(@Nullable DocumentLoader documentLoader) {
		this.documentLoader = (documentLoader != null ? documentLoader : new DefaultDocumentLoader());
	}


	public void setEntityResolver(@Nullable EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}


	protected EntityResolver getEntityResolver() {
		if (this.entityResolver == null) {
			// Determine default EntityResolver to use.
			ResourceLoader resourceLoader = getResourceLoader();
			if (resourceLoader != null) {
				this.entityResolver = new ResourceEntityResolver(resourceLoader);
			} else {
				this.entityResolver = new DelegatingEntityResolver(getBeanClassLoader());
			}
		}
		return this.entityResolver;
	}


	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}


	public void setDocumentReaderClass(Class<? extends BeanDefinitionDocumentReader> documentReaderClass) {
		this.documentReaderClass = documentReaderClass;
	}


	//1、XmlBeanDefinitionReader首先会对参数Resource 使用EncodedResource类进行封装。
	//2、获取输入流。从Resource中获取对应的InputStream并构造InputSource。
	//3、通过构造的InputSource实例和Resource实例继续调用函数doLoadBeanDefinitions。
	@Override
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		/*
		 *EncodedResource的作用是什么呢？通过名称，我们可以大致推断这个类主要是用于对资源文件的编码进行处理的。
		 *其中的主要逻辑体现在getReader()方法中，当设置了编码属性的时候 Spring会使用相应的编码作为输入流的编码。
		 */
		//这个方法内部才是真正的数据准备阶段，
		return loadBeanDefinitions(new EncodedResource(resource));
	}


	//根据编码的资源，加载XML配置的bean实例，返回找到的bean定义数量
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		Assert.notNull(encodedResource, "EncodedResource must not be null");
		if (logger.isInfoEnabled()) {
			logger.info("Loading XML bean definitions from " + encodedResource);
		}

		//将当前编码的资源存储到set集合中
		Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
		//如果set集合为null
		if (currentResources == null) {
			currentResources = new HashSet<>(4);
			this.resourcesCurrentlyBeingLoaded.set(currentResources);
		}
		if (!currentResources.add(encodedResource)) {
			throw new BeanDefinitionStoreException(
					"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
		}
		try {
			//获取spring的XML配置的输入流，该流保包含Spring配置的文件的所有信息
			InputStream inputStream = encodedResource.getResource().getInputStream();
			try {
				//SAX方式解析 xml资源输入流
				InputSource inputSource = new InputSource(inputStream);
				if (encodedResource.getEncoding() != null) {
					inputSource.setEncoding(encodedResource.getEncoding());
				}
				//根据编码资源和xml资源输入流，加载bean
				//将准备好配置文件数据(inputSource)给doLoadBeanDefinitions方法处理
				return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
			} finally {
				inputStream.close();
			}
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"IOException parsing XML document from " + encodedResource.getResource(), ex);
		} finally {
			//移除被加载过的资源文件
			currentResources.remove(encodedResource);
			if (currentResources.isEmpty()) {
				this.resourcesCurrentlyBeingLoaded.remove();
			}
		}
	}

	//从指定的XML文件加载bean定义。
	//返回bean数量
	public int loadBeanDefinitions(InputSource inputSource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(inputSource, "resource loaded through SAX InputSource");
	}


	public int loadBeanDefinitions(InputSource inputSource, @Nullable String resourceDescription)
			throws BeanDefinitionStoreException {

		return doLoadBeanDefinitions(inputSource, new DescriptiveResource(resourceDescription));
	}


	/**
	 * 从XML配置文件流中，装配bean
	 *
	 * @param inputSource 要从中读取的SAX InputSource--XML配置文件输入流
	 * @param resource    XML文件的资源描述符
	 * @return 找到的bean定义的数量
	 */
	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {
		try {
			/*
			 *这3个步骤支撑着整个Spring容器部分的实现基础，尤其是第3步对配置文件的解析，
			 */
			//加载Spring XML文件，并得到对应的Document。
			//XmlBeanFactoryReader类对于文档读取并没有亲力亲为，而是委托给了DocumentLoader去执行，
			// 这里的DocumentLoader是个接口，而真正调用的是是 DefaultDocumentLoader，
			Document doc = doLoadDocument(inputSource, resource);
			//根据返回的Document注册Bean信息。(spring基于XML配置文件的bean注册，重点)
			return registerBeanDefinitions(doc, resource);
		} catch (BeanDefinitionStoreException ex) {
			throw ex;
		} catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		} catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"XML document from " + resource + " is invalid", ex);
		} catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Parser configuration exception parsing XML from " + resource, ex);
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"IOException parsing XML document from " + resource, ex);
		} catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Unexpected exception parsing XML document from " + resource, ex);
		}
	}

	//根据Sax解析xml的流和资源流来加载解析为一个Document对象。
	protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
		//XmlBeanDefinitionReader将读取Document委托给DefaultDocumentLoader的loadDocument去读取
		return this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler,
				getValidationModeForResource(resource), isNamespaceAware());
	}

	//Spring通过getValidationModeForResource方法来获取对应资源的的验证模式。
	//主要是检验spring XML配置的DTD(Document)和XSD(XML)
	protected int getValidationModeForResource(Resource resource) {
		int validationModeToUse = getValidationMode();
		//手动指定检验模式
		//如果手动指定了验证模式则使用指定的验证模式
		if (validationModeToUse != VALIDATION_AUTO) {
			return validationModeToUse;
		}
		//spring自动检验方式（默认方式）
		//自动检测的方式。而自动检测验证模式的功能是在函 detectValidationMode 方法中实现的，
		// 在 detectValidationMode 函数中又将自动检测验证模式的工作委托给了专门处理类XmlValidationMode Detector，
		// 调用了XmlValidationModeDetector的validationModeDetector方法，
		int detectedMode = detectValidationMode(resource);
		if (detectedMode != VALIDATION_AUTO) {
			return detectedMode;
		}
		// Hmm, we didn't get a clear indication... Let's assume XSD,
		// since apparently no DTD declaration has been found up until
		// detection stopped (before finding the document's root tag).
		return VALIDATION_XSD;
	}

	/**
	 * Detect which kind of validation to perform on the XML file identified
	 * by the supplied {@link Resource}. If the file has a {@code DOCTYPE}
	 * definition then DTD validation is used otherwise XSD validation is assumed.
	 * <p>Override this method if you would like to customize resolution
	 * of the {@link #VALIDATION_AUTO} mode.
	 */
	protected int detectValidationMode(Resource resource) {
		if (resource.isOpen()) {
			throw new BeanDefinitionStoreException(
					"Passed-in Resource [" + resource + "] contains an open stream: " +
							"cannot determine validation mode automatically. Either pass in a Resource " +
							"that is able to create fresh streams, or explicitly specify the validationMode " +
							"on your XmlBeanDefinitionReader instance.");
		}

		InputStream inputStream;
		try {
			inputStream = resource.getInputStream();
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"Unable to determine validation mode for [" + resource + "]: cannot open InputStream. " +
							"Did you attempt to load directly from a SAX InputSource without specifying the " +
							"validationMode on your XmlBeanDefinitionReader instance?", ex);
		}

		try {
			return this.validationModeDetector.detectValidationMode(inputStream);
		} catch (IOException ex) {
			throw new BeanDefinitionStoreException("Unable to determine validation mode for [" +
					resource + "]: an error occurred whilst reading from the InputStream.", ex);
		}
	}

	/*
	 *registerBeanDefinitions方法：将spring的XMl配置解析成Document对象，和Spring的xml配置Resource子类对象给BeanDefinitionDocumentReader
	 * 对象进行读取bean
	 * doc 是 通 过 上 一 节 loadDocument 加 载 转 换 出 来 的。
	 */
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		//获取DocumentReader文档读取器DefaultBeanDefinitionDocumentReader(BeanDefinitionDocumentReader的实现类)，去读取Document对象
		//创建BeanDefinitionDocumentReader-bean定义的Document对象的读取器
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		//在实例化BeanDefinitionReader时候会将BeanDefinitionRegistry传入，
		// 默认使用继承自DefaultListableBeanFactory 的子类 　
		// 记录统计前 BeanDefinition 的加载个数
		int countBefore = getRegistry().getBeanDefinitionCount();
		//将XML配置文件中bean装配到IOC容器
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		//返回装配的到IOC容器的bean数量
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}

	//创建DefaultBeanDefinitionDocumentReader
	protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
		return BeanUtils.instantiateClass(this.documentReaderClass);
	}

	/**
	 * Create the {@link XmlReaderContext} to pass over to the document reader.
	 */
	public XmlReaderContext createReaderContext(Resource resource) {
		return new XmlReaderContext(resource, this.problemReporter, this.eventListener,
				this.sourceExtractor, this, getNamespaceHandlerResolver());
	}

	/**
	 * Lazily create a default NamespaceHandlerResolver, if not set before.
	 *
	 * @see #createDefaultNamespaceHandlerResolver()
	 */
	public NamespaceHandlerResolver getNamespaceHandlerResolver() {
		if (this.namespaceHandlerResolver == null) {
			this.namespaceHandlerResolver = createDefaultNamespaceHandlerResolver();
		}
		return this.namespaceHandlerResolver;
	}

	/**
	 * Create the default implementation of {@link NamespaceHandlerResolver} used if none is specified.
	 * <p>The default implementation returns an instance of {@link DefaultNamespaceHandlerResolver}.
	 *
	 * @see DefaultNamespaceHandlerResolver#DefaultNamespaceHandlerResolver(ClassLoader)
	 */
	protected NamespaceHandlerResolver createDefaultNamespaceHandlerResolver() {
		ClassLoader cl = (getResourceLoader() != null ? getResourceLoader().getClassLoader() : getBeanClassLoader());
		return new DefaultNamespaceHandlerResolver(cl);
	}

}
