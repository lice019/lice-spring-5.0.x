package com.lice.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * description: AppConfig <br>
 * date: 2019/10/15 14:59 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
@Configuration
@ComponentScan("com.lice")
@EnableAspectJAutoProxy
public class AppConfig implements WebMvcConfigurer {



}
