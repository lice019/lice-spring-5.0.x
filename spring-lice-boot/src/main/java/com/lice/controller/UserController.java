package com.lice.controller;

import com.lice.entity.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * description: UserController <br>
 * date: 2019/10/15 15:05 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
@RestController
@RequestMapping("user")
public class UserController {


	@RequestMapping("/query")
	@ResponseBody
	public String  query() {
		System.out.println("user controller id executed......");
		return "hello";
	}
}
