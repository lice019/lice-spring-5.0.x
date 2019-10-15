
package org.springframework.context.support;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * AbstractRefreshableConfigApplicationContext：可执行AbstractApplicationContext类中refresh()方法的
 * 的上下文类
 *
 * @see #setConfigLocation
 * @see #setConfigLocations
 * @see #getDefaultConfigLocations
 */
public abstract class AbstractRefreshableConfigApplicationContext extends AbstractRefreshableApplicationContext
		implements BeanNameAware, InitializingBean {

	//配置文件路径，是一个数组，因为spring可以配置多个xml配置文件
	@Nullable
	private String[] configLocations;

	private boolean setIdCalled = false;


	//创建一个新的没有父类的abstractrefbleconfigapplicationcontext。
	public AbstractRefreshableConfigApplicationContext() {
	}


	public AbstractRefreshableConfigApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	public void setConfigLocation(String location) {
		setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS));
	}


	/*
	 *ApplicationContext初始化IOC容器设置配置文件的路径
	 */
	public void setConfigLocations(@Nullable String... locations) {
		if (locations != null) {
			Assert.noNullElements(locations, "Config locations must not be null");


			this.configLocations = new String[locations.length];
			//遍历配置文件（locations是可变参数）
			for (int i = 0; i < locations.length; i++) {
				//解析给定配置文件路径
				//resolvePath(locations[i])会搜索匹配系统变量并替换
				this.configLocations[i] = resolvePath(locations[i]).trim();
			}
		} else {
			this.configLocations = null;
		}
	}


	@Nullable
	protected String[] getConfigLocations() {
		return (this.configLocations != null ? this.configLocations : getDefaultConfigLocations());
	}


	@Nullable
	protected String[] getDefaultConfigLocations() {
		return null;
	}

	//解析配置文件的路径
	protected String resolvePath(String path) {
		return getEnvironment().resolveRequiredPlaceholders(path);
	}


	@Override
	public void setId(String id) {
		super.setId(id);
		this.setIdCalled = true;
	}

	//默认情况下，将此上下文的id设置为bean名称，用于上下文实例本身定义为bean的情况。
	@Override
	public void setBeanName(String name) {
		if (!this.setIdCalled) {
			super.setId(name);
			setDisplayName("ApplicationContext '" + name + "'");
		}
	}

	//如果尚未在具体上下文的构造函数中刷新，则触发{@link #refresh()}。
	@Override
	public void afterPropertiesSet() {
		if (!isActive()) {
			refresh();
		}
	}

}
