package com.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan("com.spring.aop")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopConfig {

//	@Bean
//	public LogAspect logAspect() {
//		return new LogAspect();
//	}

}
