package com.lice.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * description: WebConfig <br>
 * date: 2019/10/15 15:59 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
//@Configuration
//@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	//添加spring mvc的消息转换器，处理JSON转换
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new FastJsonHttpMessageConverter());
	}
}
