package com.liuqihang.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@ComponentScan("com.liuqihang.selftag")
public class MyComponentScan {

    @ComponentScan("com.liuqihang.selftag")
    @Configuration
    @Order(90)
    class InnerClass{

    }

}
