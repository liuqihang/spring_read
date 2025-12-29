package com.spring.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test01 {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test.xml");
		Person bean = context.getBean(Person.class);
		System.out.println(bean);
		System.out.println("Hello Spring");
	}
}
