
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.lang.Nullable;

/**
 * ThemeResolver：解决Web应用程序可以使用的主题，例如，提供个性化的布局。
 *
 * @see org.springframework.ui.context.Theme
 * @see org.springframework.ui.context.ThemeSource
 */
public interface ThemeResolver {

	//通过给定的请求解析当前的主题名。
	//在任何情况下都应该返回默认主题作为回退。
	String resolveThemeName(HttpServletRequest request);

	//将当前主题名设置为给定的主题名。
	void setThemeName(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable String themeName);

}
