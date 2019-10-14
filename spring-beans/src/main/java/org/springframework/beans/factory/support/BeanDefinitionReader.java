package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;

//spring的bean定义读取器接口规范
public interface BeanDefinitionReader {

	//返回bean工厂来注册bean定义。工厂通过BeanDefinitionRegistry接口公开，封装与bean定义处理相关的方法。
	BeanDefinitionRegistry getRegistry();

	//获取资源加载器
	@Nullable
	ResourceLoader getResourceLoader();

	//获取类加载器
	@Nullable
	ClassLoader getBeanClassLoader();

	//获取bean名称生成器
	BeanNameGenerator getBeanNameGenerator();


	//从指定资源加载bean定义。
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;

	//从指定资源加载bean定义。
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;

	//从指定的资源位置加载bean定义。
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;

	//从指定的资源位置加载bean定义。
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
