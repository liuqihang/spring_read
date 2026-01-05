package com.spring.tx;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TxMain {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(TxConfig.class);

		EmpService empService = ctx.getBean(EmpService.class);

		System.out.println("Bean class = " + empService.getClass());

		empService.createUser();
	}
}
