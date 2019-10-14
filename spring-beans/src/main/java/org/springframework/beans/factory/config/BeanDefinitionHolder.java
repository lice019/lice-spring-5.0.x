
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * BeanDefinitionHolder，简单来说其就是一个BeanDefinition的持有者，其定义了一些变量，并对以下变量提供get和set操作。
 * @see org.springframework.beans.factory.BeanNameAware
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public class BeanDefinitionHolder implements BeanMetadataElement {

	//封装bean相信的对象，BeanDefinition
	private final BeanDefinition beanDefinition;

	//bean的名称
	private final String beanName;

	//bean的别名
	@Nullable
	private final String[] aliases;



	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}


	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, @Nullable String[] aliases) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}


	public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
		Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
		this.beanName = beanDefinitionHolder.getBeanName();
		this.aliases = beanDefinitionHolder.getAliases();
	}



	//返回包装的BeanDefinition
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}

	//返回bean的主名称，如为bean定义指定的那样。
	public String getBeanName() {
		return this.beanName;
	}

	//返回bean的别名，直接为bean定义指定。@返回别名数组，如果没有别名，则返回{@code null}
	@Nullable
	public String[] getAliases() {
		return this.aliases;
	}

	/**
	 * 公开bean定义的源对象。
	 * @see BeanDefinition#getSource()
	 */
	@Override
	@Nullable
	public Object getSource() {
		return this.beanDefinition.getSource();
	}

	//确定给定的候选名称是否与此bean定义中存储的bean名称或别名匹配。
	public boolean matchesName(@Nullable String candidateName) {
		return (candidateName != null && (candidateName.equals(this.beanName) ||
				candidateName.equals(BeanFactoryUtils.transformedBeanName(this.beanName)) ||
				ObjectUtils.containsElement(this.aliases, candidateName)));
	}



	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bean definition with name '").append(this.beanName).append("'");
		if (this.aliases != null) {
			sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
		}
		return sb.toString();
	}


	public String getLongDescription() {
		StringBuilder sb = new StringBuilder(getShortDescription());
		sb.append(": ").append(this.beanDefinition);
		return sb.toString();
	}


	@Override
	public String toString() {
		return getLongDescription();
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanDefinitionHolder)) {
			return false;
		}
		BeanDefinitionHolder otherHolder = (BeanDefinitionHolder) other;
		return this.beanDefinition.equals(otherHolder.beanDefinition) &&
				this.beanName.equals(otherHolder.beanName) &&
				ObjectUtils.nullSafeEquals(this.aliases, otherHolder.aliases);
	}

	@Override
	public int hashCode() {
		int hashCode = this.beanDefinition.hashCode();
		hashCode = 29 * hashCode + this.beanName.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
		return hashCode;
	}

}
