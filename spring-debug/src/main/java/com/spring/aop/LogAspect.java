package com.spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

//	@Pointcut("execution(* com.spring.aop..*(..))")
	@Pointcut("execution(* com.spring.aop.LogService.*(..))")
	public void serviceMethods() {}


	@Before("serviceMethods()")
	public void before() {
		System.out.println("[LogAspect] before");
	}

	@Around("serviceMethods()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("[LogAspect] around before");
		Object ret = pjp.proceed();
		System.out.println("[LogAspect] around after");
		return ret;
	}


}
