package com.lice;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 * description: App <br>
 * date: 2019/10/15 14:49 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class App {
	public static void main(String[] args) {

		//初始化tomcat
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(8089);
		tomcat.addWebapp("/", "d:\\log\\");

		try {
			//启动tomcat
			tomcat.start();
			//让tomcat一直等待，如果不等待，start()执行完，tomcat就停止了
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}


	}
}
