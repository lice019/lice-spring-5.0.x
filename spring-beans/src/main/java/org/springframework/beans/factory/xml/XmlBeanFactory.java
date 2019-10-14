
package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

/**
 * XmlBeanFactory（以过时）：spring的XML配置使用的bean工厂，继承了最强大的bean工厂DefaultListableBeanFactory，
 * XmlBeanFactory对DefaultListableBeanFactory类进行了扩展，主要用于从XML文档中读取BeanDefinition，
 * 对于注册及获取Bean都是使用从父类 DefaultListableBeanFactory 继承的方法去实现，而唯独与父类不同的个性化
 * 实现就是增加了 XmlBeanDefinitionReader 类型的 reader 属性。 在 XmlBeanFactory 中主要使用 reader
 * 属性对资源文件进行读取和注册。
 *
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see XmlBeanDefinitionReader
 * @deprecated as of Spring 3.1 in favor of {@link DefaultListableBeanFactory} and
 * {@link XmlBeanDefinitionReader}
 */
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {


	//Spring的XML配置的bean的读取器，读取ApplicationContext.xml中的bean定义和注册bean
	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);



	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}


	/*
	*当通过Resource相关类完成了对配置文件进行封装后配置文件的读取工作就全权交给XmlBeanDefinitionReader来处理了。
	 */
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		//parentBeanFactory)，跟踪代码到父类AbstractAutowireCapableBeanFactory的构造函数中：

		super(parentBeanFactory);
		//XmlBeanDefinitionReader来读取bean定义
		//this.reader.loadBeanDefinitions( resource)才是资源加载的真正实现，
		//1、XmlBeanDefinitionReader首先会对参数Resource 使用EncodedResource类进行封装。
		//2、获取输入流。从Resource中获取对应的InputStream并构造InputSource。
		//3、通过构造的InputSource实例和Resource实例继续调用函数doLoadBeanDefinitions。
		this.reader.loadBeanDefinitions(resource);
	}

}
