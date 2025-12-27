package com.liuqihang.aop.annotation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan(basePackages="com.liuqihang.aop.annotation")
@EnableAspectJAutoProxy
public class SpringConfiguration { }