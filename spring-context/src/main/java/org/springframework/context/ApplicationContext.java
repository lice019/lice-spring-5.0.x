package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

/**
 * ApplicationContext：为应用程序提供配置的中央接口。提供给用户操作，ApplicationContext对BeanFactory的所有功能进行了扩展
 * 也就是ApplicationContext比BeanFactory功能强大，通常建议ApplicationContext比BeanFactory优先，而且ApplicationContext
 * 会对字节的长度有很大的影响，企业级应用就是需要ApplicationContext来操作Spring
 * 而且ApplicationContext加载配置方式不同：
 * 1、ApplicationContext加载方式：
 * 		ApplicationContext bf = new ClassPathXmlApplicationContext(" beanFactoryTest.xml");
 * 2、BeanFactory加载方式：
 * 		BeanFactory bf = new XmlBeanFactory( new ClassPathResource(" beanFactoryTest.xml"));
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {


	@Nullable
	String getId();


	String getApplicationName();


	String getDisplayName();


	long getStartupDate();


	@Nullable
	ApplicationContext getParent();

	//获取自动扩容的Bean工厂
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
