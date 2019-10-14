package org.springframework.core.type;

import java.util.Set;

/**
 * 定义对特定注释的抽象访问的接口类，其形式不要求加载该类。
 */

/**
 * 元注解：相当于父类，某些注解是在元注解的基础上扩展出来的，典型的元注解是@Component，@Service、@Controller扩展了@Component注解
 */
public interface AnnotationMetadata extends ClassMetadata, AnnotatedTypeMetadata {


	//获取底层类上所有表示的的注释类型的完全限定类名。
	Set<String> getAnnotationTypes();

	//获得annottationName对应的元注解的类全限定名
	Set<String> getMetaAnnotationTypes(String annotationName);

	//确定是否含有某个注解
	boolean hasAnnotation(String annotationName);

	//确定是否含有某个元注解
	boolean hasMetaAnnotation(String metaAnnotationName);

	//确定类的方法是否含有某个注解
	boolean hasAnnotatedMethods(String annotationName);

	//返回类中所有被注解注释的方法
	Set<MethodMetadata> getAnnotatedMethods(String annotationName);

}
