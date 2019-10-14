
package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStreamSource封装了任何能返回InputStream的类，比如file，classpath下的资源以及ByteArray等。
 * 对于不同的来源的资源文件都有想对应的Resource实现。最常用的就比如ClassPath资源（ClassPathResource），file资源（FileSystemResource），URL资源（UrlResource），Byte数组（ByteArrayResource）等等。
 * <p>
 * 有了Resource接口便可以对所有的资源文件进行统一处理。
 * 如下：通过ClassPathResource读取根目录下的spring.xml,通过XmlBeanFactoryXmlBeanFactory的构造函数间的调用，将Resource交由XmlBeanDefinitionReader对配置文件进行读取。
 *
 * @see java.io.InputStream
 * @see Resource
 * @see InputStreamResource
 * @see ByteArrayResource
 */
public interface InputStreamSource {

	//返回一个输入流
	InputStream getInputStream() throws IOException;
	/*
	 *InputStreamSource 封装任何能返回InputStream的类， 比如File、Classpath下的资源和Byte Array等。
	 * 它只有一个方法定义： getInputStream()，
	 * 该方法返回一个新的InputStream对象。
	 */

}
