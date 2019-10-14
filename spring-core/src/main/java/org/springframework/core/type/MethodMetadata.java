
package org.springframework.core.type;

/**
 * 接口，该接口定义对特定类的注释的抽象访问，其形式不需要加载该类。
 */
//MethodMetadata:方法元数据，
public interface MethodMetadata extends AnnotatedTypeMetadata {

	//返回方法的名字
	String getMethodName();

	//返回该方法所属的类的全限定名
	String getDeclaringClassName();

	//返回该方法返回类型的全限定名
	String getReturnTypeName();

	//以下接口判断方法是不是抽象、静态、final、override
	boolean isAbstract();
	boolean isStatic();
	boolean isFinal();
	boolean isOverridable();

}
