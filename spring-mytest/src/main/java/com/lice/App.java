package com.lice;

import com.lice.AOP.AOPService;
import com.lice.AOP.MyAspect;
import com.lice.bean.Student;
import com.lice.config.AppConfig;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Date;

/**
 * description: spring App测试类 <br>
 * date: 2019/8/17 11:00 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class App {

	public static void main(String[] args) {

		//初始化spring的IOC容器
		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
//		Student stu = ac.getBean(Student.class);
//		stu.setStuId("0806160250");
//		stu.setStuName("杨小芹");
//		stu.setAddress("潮汕揭阳");
//		stu.setBirthday(new Date());
//		System.out.println(stu);
//		System.out.println("我是一名程序员");
		//AOP
		AOPService aopService = ac.getBean(AOPService.class);
		aopService.pointCut();

//		GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
//		genericBeanDefinition.setBeanClass(Student.class);

	}
}
