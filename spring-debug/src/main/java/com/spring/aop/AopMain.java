//package com.spring.aop;
//
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//
//public class AopMain {
//
//	public static void main(String[] args) {
//		AnnotationConfigApplicationContext ctx =
//				new AnnotationConfigApplicationContext(AopConfig.class);
////				new AnnotationConfigApplicationContext();
//
////		AopConfig ac = ctx.getBean(AopConfig.class);
////		System.out.println("ac Bean class = " + ac.getClass());
////		LogAspect la = ctx.getBean(LogAspect.class);
////		System.out.println("la Bean class = " + la.getClass());
////		MyBeanAware mba = ctx.getBean(MyBeanAware.class);
////		System.out.println("mba Bean class = " + mba.getClass());
//
//		ILogService logService = (ILogService) ctx.getBean("logService");
////		ILogService logService = (ILogService) ctx.getBean(LogService.class);
//		System.out.println("logService Bean class = " + logService.getClass());
//
//
//		logService.printLog();
//		logService.auth();
//
//		LifecycleTest lt = ctx.getBean(LifecycleTest.class);
//		lt.sayHello();
//	}
//}
