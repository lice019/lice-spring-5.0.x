package org.springframework.context.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.context.annotation.AnnotationConfigBeanDefinitionParser;
import org.springframework.context.annotation.ComponentScanBeanDefinitionParser;

/**
 * {@link org.springframework.beans.factory.xml.NamespaceHandler}
 * for the '{@code context}' namespace.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
//ContextNamespaceHandler是用来解析spring.xml配置文件中context命名空间下的节点的。
public class ContextNamespaceHandler extends NamespaceHandlerSupport {

	//初始化加载过程，相应的标签element，注册相应的解析器
	@Override
	public void init() {
		registerBeanDefinitionParser("property-placeholder", new PropertyPlaceholderBeanDefinitionParser());
		registerBeanDefinitionParser("property-override", new PropertyOverrideBeanDefinitionParser());
		registerBeanDefinitionParser("annotation-config", new AnnotationConfigBeanDefinitionParser());
		//扫描注解开启，通过ComponentScanBeanDefinitionParser解析器
		registerBeanDefinitionParser("component-scan", new ComponentScanBeanDefinitionParser());
		registerBeanDefinitionParser("load-time-weaver", new LoadTimeWeaverBeanDefinitionParser());
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
		registerBeanDefinitionParser("mbean-export", new MBeanExportBeanDefinitionParser());
		registerBeanDefinitionParser("mbean-server", new MBeanServerBeanDefinitionParser());
	}

}
