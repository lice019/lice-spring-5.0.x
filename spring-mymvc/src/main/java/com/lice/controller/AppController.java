package com.lice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * description: AppController <br>
 * date: 2019/8/27 9:57 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
@Controller
public class AppController {

	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "Hello mvc";
	}
}
