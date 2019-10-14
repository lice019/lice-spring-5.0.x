package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.lang.Nullable;



/**
 * GenericBeanDefinition：继承于AbstractBeanDefinition，拓展AbstractBeanDefinition的不足。用于存储XML配置的bean
 */
@SuppressWarnings("serial")
public class GenericBeanDefinition extends AbstractBeanDefinition {

	//bean父类的名称
	@Nullable
	private String parentName;


	//初始化，先实例化父类AbstractBeanDefinition
	public GenericBeanDefinition() {
		super();
	}


	public GenericBeanDefinition(BeanDefinition original) {
		super(original);
	}

	//设置父类的名称
	@Override
	public void setParentName(@Nullable String parentName) {
		this.parentName = parentName;
	}

	@Override
	@Nullable
	public String getParentName() {
		return this.parentName;
	}


	//克隆bean
	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new GenericBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof GenericBeanDefinition && super.equals(other)));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Generic bean");
		if (this.parentName != null) {
			sb.append(" with parent '").append(this.parentName).append("'");
		}
		sb.append(": ").append(super.toString());
		return sb.toString();
	}

}
