package com.lice.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * description: MyAspect <br>
 * date: 2019/10/3 22:20 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
@Component
@Aspect
public class MyAspect {


	@Before("execution(* com.lice.AOP.AOPService.*(..))")
	public void before() {
		System.out.println("spring aop的前置通知....");
	}

	@After("execution(* com.lice.AOP.AOPService.*(..))")
	public void after() {
		System.out.println("spring aop的后置通知.....");
	}

	@Around("execution(* com.lice.AOP.AOPService.*(..))")
	public Object around(ProceedingJoinPoint p) {
		System.out.println(" before1");
		Object o = null;
		try {
			o = p.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println(" after1");
		return o;

	}
}
