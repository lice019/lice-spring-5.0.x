
package org.springframework.core;

/**
 * 用于管理别名的通用接口。作为超级接口
 * {@link org.springframework.beans.factory.support.BeanDefinitionRegistry}。
 */
public interface AliasRegistry {

	//给name注册别名
	void registerAlias(String name, String alias);

	//移除指定的别名
	void removeAlias(String alias);

	//确定此给定名称是否定义为别名(与实际注册组件的名称相反)。
	boolean isAlias(String name);

	//获取给定的名称的所有别名
	String[] getAliases(String name);

}
