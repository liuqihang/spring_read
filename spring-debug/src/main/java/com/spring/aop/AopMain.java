package com.spring.aop;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AopMain {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(AopConfig.class);

		LogService logService = ctx.getBean(LogService.class);

		System.out.println("Bean class = " + logService.getClass());
		logService.printLog();
	}
}
