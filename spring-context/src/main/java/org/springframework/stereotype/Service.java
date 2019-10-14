
package org.springframework.stereotype;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * 表明一个带注释的类是一个“服务”，最初由域驱动设计(Evans, 2003)定义为“一个操作作为一个独立于模型的接口提供，没有封装状态。”
 * <p>
 * 还可能指示类是“业务服务Facade”(在核心J2EE模式意义上)，或类似的东西。这个注释是一个通用的原型，单个的团队可能会缩小他们的语义并在适当的时候使用。
 * <p>
 * 此注释作为{@link Component @Component}的专门化，允许通过类路径扫描自动检测实现类。
 *
 * @author Juergen Hoeller
 * @see Component
 * @see Repository
 * @since 2.5
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {

	/**
	 * 该值可能表示对逻辑组件名称的建议，
	 * <p>
	 * 在自动检测组件的情况下转换为Spring bean。
	 *
	 * @返回建议的组件名，如果有的话(或者空字符串，否则)
	 */
	@AliasFor(annotation = Component.class)
	String value() default "";

}
