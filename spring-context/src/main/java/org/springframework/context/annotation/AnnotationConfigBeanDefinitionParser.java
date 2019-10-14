
package org.springframework.context.annotation;

import java.util.Set;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;

/**
 * < context:annotation-config/ >的解析器元素。
 *
 * @see AnnotationConfigUtils
 */
public class AnnotationConfigBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		Object source = parserContext.extractSource(element);

		// Obtain bean definitions for all relevant BeanPostProcessors.
		Set<BeanDefinitionHolder> processorDefinitions =
				AnnotationConfigUtils.registerAnnotationConfigProcessors(parserContext.getRegistry(), source);

		// Register component for the surrounding <context:annotation-config> element.
		CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
		parserContext.pushContainingComponent(compDefinition);

		// Nest the concrete beans in the surrounding component.
		for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
			parserContext.registerComponent(new BeanComponentDefinition(processorDefinition));
		}

		// Finally register the composite component.
		parserContext.popAndRegisterContainingComponent();

		return null;
	}

}
