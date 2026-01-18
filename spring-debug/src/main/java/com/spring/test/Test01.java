package com.spring.test;

import com.spring.service.IUserService;
import com.spring.service.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test01 {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test.xml");
//		MybatisConfig mc = context.getBean(MybatisConfig.class);
//		System.out.println(mc);


//		IUserService us = (IUserService)context.getBean(UserService.class);
//		IUserService us = (IUserService)context.getBean(UserService.class);
//		System.out.println(us);
//		us.sayHello();


//		Person bean = context.getBean(Person.class);
//		System.out.println(bean.getName());
//		System.out.println("Hello Spring");
	}
}
