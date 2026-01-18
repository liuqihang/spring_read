package com.spring.aop;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

//Configuration的注解Spring也会为其做CGLIB代理，为了保证 @Bean 语义，
// 在BeanFactoryPostProcessor
//--> invokeBeanFactoryPostProcessors
//--> ConfigurationClassPostProcessor.postProcessBeanFactory()
//--> enhanceConfigurationClasses(beanFactory);
//--> if (ConfigurationClassUtils.isFullConfigurationClass(bd)) {
//		// 必须 CGLIB 增强
//		}
// 真正发生代理的代码：ConfigurationClassEnhancer.enhance()
/*
* Spring 的解决方案

👉 用 CGLIB 代理 @Configuration 类

调用 b()
  ↓
调用 a()（被 CGLIB 拦截）
  ↓
转为 BeanFactory.getBean("a")
这就是 “配置类增强” 的本质。
* */

@Configuration
@ComponentScan("com.spring.aop")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopConfig {

	// 注册UserBean，指定初始化和销毁方法
	@Bean(initMethod = "myInitMethod", destroyMethod = "myDestroyMethod")
	public LifecycleTest userBean() {
		LifecycleTest userBean = new LifecycleTest();
		userBean.setLifeCycle("Spring learner");
		return userBean;
	}

}
