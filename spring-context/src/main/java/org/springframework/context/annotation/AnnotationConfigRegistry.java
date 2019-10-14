package org.springframework.context.annotation;

/**
 * 用于注释配置应用程序上下文的公共接口，
 * 定义{@link #register}和{@link #scan}方法。
 *
 * @author Juergen Hoeller
 * @since 4.1
 */
public interface AnnotationConfigRegistry {

	/**
	 *注册一个或多个要处理的带注释的类。
	 *调用{@code register}是幂等的;多次添加同一个带注释的类不会产生额外的效果。
	 *@param注释类一个或多个注释类，
	 *例如{@link Configuration @Configuration}类
	 */
	void register(Class<?>... annotatedClasses);

	/**
	 * 在指定的基本包中执行扫描。
	 *
	 * @param basePackages包检查带注释的类
	 * 注解有：@Component @service等注解
	 */
	void scan(String... basePackages);

}
