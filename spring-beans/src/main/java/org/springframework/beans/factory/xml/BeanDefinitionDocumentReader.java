package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;

/**
 * BeanDefinitionDocumentReader：通过解析<bean></bean>的读取，来将bean存储在IOC中
 * BeanDefinitionDocumentReader： 定 义 读 取 Docuemnt 并 注 册 BeanDefinition 功 能。
 *
 */
public interface BeanDefinitionDocumentReader {


	//注册Bean到注册中心和IOC中
	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext)
			throws BeanDefinitionStoreException;

}
