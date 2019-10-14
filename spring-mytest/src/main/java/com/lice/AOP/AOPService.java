package com.lice.AOP;

import org.springframework.stereotype.Service;

/**
 * description: AOPService <br>
 * date: 2019/10/3 23:06 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
@Service
public class AOPService {

	//被拦截的方法
	public void pointCut(){
		System.out.println("AOPService pointCut is Executed.....");
	}
}
