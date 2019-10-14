/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet;

import java.util.Locale;

import org.springframework.lang.Nullable;

/**
 * ViewResolver:视图解析器，用于解析处理器适配返回的ModelAndView对象，将模型数据解析到指定的视图并渲染
 *
 * 可以按名称解析视图的对象实现的接口。
 * 视图状态在应用程序运行期间不改变，
 * 因此实现可以自由地缓存视图。
 * 实施被鼓励支持国际化，
 * 即本地化视图分辨率。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.servlet.view.InternalResourceViewResolver
 * @see org.springframework.web.servlet.view.ResourceBundleViewResolver
 * @see org.springframework.web.servlet.view.XmlViewResolver
 */
public interface ViewResolver {


	//按名称解析给定的视图。返回view视图对象
	@Nullable
	View resolveViewName(String viewName, Locale locale) throws Exception;

}
