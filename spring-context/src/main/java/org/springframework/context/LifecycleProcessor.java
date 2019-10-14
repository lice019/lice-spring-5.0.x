package org.springframework.context;

/**
 * 在ApplicationContext中处理生命周期bean的策略接口。
 *
 */
public interface LifecycleProcessor extends Lifecycle {

	//上下文刷新通知，例如用于自动启动组件。
	void onRefresh();

	//上下文关闭阶段的通知，例如自动停止组件。
	void onClose();

}
