package com.spring.aop;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LifecycleTest  implements
		BeanNameAware,
		BeanClassLoaderAware,
		BeanFactoryAware,
		ApplicationContextAware,
		InitializingBean,
		DisposableBean {

	private String lifeCycle;

	// 无参构造器(实例化时调用)
	public LifecycleTest() {
		System.out.println("1. Bean instantiation:invoke no param constructor create object ======");
	}

	// Setter方法(依赖注入时调用)
	public void setLifeCycle(String lifeCycle) {
		this.lifeCycle = lifeCycle;
		System.out.println("2. set Bean attribute:DI lifeCycle = " + lifeCycle + " ======");
	}

	// ==== Aware 接口方法 ====
	@Override
	public void setBeanName(String name) {
		System.out.println("3. BeanNameAware: set Bean name = " + name + " ======");
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		System.out.println("4. BeanClassLoaderAware:set ClassLoader ======");
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		System.out.println("5. BeanFactoryAware: set BeanFactory ======");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		System.out.println("6. ApplicationContextAware:set ApplicationContext ======");
	}

	// ==== 初始化方法 ====
	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("8. InitializingBean:invoke afterPropertiesSet(init method) ======");
	}

	// 自定义初始化方法(通过配置指定)
	public void myInitMethod() {
		System.out.println("9. custom init-method:execute custom init logic ======");
	}

	// ==== 销毁方法 ====
	@Override
	public void destroy() throws Exception {
		System.out.println("container close - 1. DisposableBean: invoke destroy(destroy method) ======");
	}

	// 自定义销毁方法(通过配置指定)
	public void myDestroyMethod() {
		System.out.println("container close - 2. custom destroy-method:execute custom destroy logic ======");
	}

	// 业务方法(Bean就绪后调用)
	public void sayHello() {
		System.out.println("Bean ready :Hello, " + lifeCycle + " ======");
	}
}
