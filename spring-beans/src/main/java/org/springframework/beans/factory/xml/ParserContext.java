package org.springframework.beans.factory.xml;

import java.util.ArrayDeque;
import java.util.Deque;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.lang.Nullable;

/**
 * 通过bean定义解析过程传递的上下文，
 * 封装所有相关的配置和状态。
 * 嵌套在{@link XmlReaderContext}内。
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @see XmlReaderContext
 * @see BeanDefinitionParserDelegate
 * @since 2.0
 */
//使用了构建模式
public final class ParserContext {

	//XML上下文读取器
	private final XmlReaderContext readerContext;

	//委托对象
	private final BeanDefinitionParserDelegate delegate;

	//容器中的bean
	@Nullable
	private BeanDefinition containingBeanDefinition;

	//装载混合组件bean容器
	private final Deque<CompositeComponentDefinition> containingComponents = new ArrayDeque<>();


	//ParserContext初始化
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate) {
		//传入上下文读取器
		this.readerContext = readerContext;
		//传入委托者
		this.delegate = delegate;
	}

	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate,
						 @Nullable BeanDefinition containingBeanDefinition) {

		this.readerContext = readerContext;
		this.delegate = delegate;
		//bean
		this.containingBeanDefinition = containingBeanDefinition;
	}


	public final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	public final BeanDefinitionRegistry getRegistry() {
		return this.readerContext.getRegistry();
	}

	public final BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}

	@Nullable
	public final BeanDefinition getContainingBeanDefinition() {
		return this.containingBeanDefinition;
	}

	//containingBeanDefinition是否为null
	public final boolean isNested() {
		return (this.containingBeanDefinition != null);
	}

	//是否懒加载
	public boolean isDefaultLazyInit() {
		return BeanDefinitionParserDelegate.TRUE_VALUE.equals(this.delegate.getDefaults().getLazyInit());
	}

	@Nullable
	public Object extractSource(Object sourceCandidate) {
		return this.readerContext.extractSource(sourceCandidate);
	}


	//从队列中获取顶层CompositeComponentDefinition，但不移除
	@Nullable
	public CompositeComponentDefinition getContainingComponent() {
		return this.containingComponents.peek();
	}

	//添加
	public void pushContainingComponent(CompositeComponentDefinition containingComponent) {
		this.containingComponents.push(containingComponent);
	}

	public CompositeComponentDefinition popContainingComponent() {
		return this.containingComponents.pop();
	}

	//添加并注册
	public void popAndRegisterContainingComponent() {
		registerComponent(popContainingComponent());
	}

	//注册
	public void registerComponent(ComponentDefinition component) {
		CompositeComponentDefinition containingComponent = getContainingComponent();
		if (containingComponent != null) {
			containingComponent.addNestedComponent(component);
		} else {
			this.readerContext.fireComponentRegistered(component);
		}
	}

	public void registerBeanComponent(BeanComponentDefinition component) {
		BeanDefinitionReaderUtils.registerBeanDefinition(component, getRegistry());
		registerComponent(component);
	}

}
