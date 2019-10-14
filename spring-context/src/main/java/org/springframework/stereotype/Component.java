
package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指示带注释的类是“组件”。
 * 当使用基于注释的配置和类路径扫描时，这些类被认为是自动检测的候选类。
 * 其他类级别的注释可以被认为是可识别的
 * 一个组件，通常是一种特殊的组件:
 * 例如{@link Repository @Repository}注释或AspectJ的
 * {@link org.aspectj.lang.annotation。方面@Aspect}注释。
 *
 * @author Mark Fisher
 * @see Repository
 * @see Service
 * @see Controller
 * @see org.springframework.context.annotation.ClassPathBeanDefinitionScanner
 * @since 2.5
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
public @interface Component {

	//Component组件的名称设置
	String value() default "";

}
