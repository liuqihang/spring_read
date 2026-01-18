package com.spring.at;

import com.spring.at.config.AtConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AopAndTxBeanTest {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(AtConfig.class);
		// 1. 打印所有 BeanDefinition 名称（能看到 importRegistry）
		System.out.println("=== BeanDefinition ===");
		for (String beanName : context.getBeanFactory().getBeanDefinitionNames()) {
			System.out.println(beanName);
		}

		// 2. 打印已实例化的单例 Bean（看不到 importRegistry，因为是懒加载）
		System.out.println("--- print singletonName ---");
		for (String beanName : context.getBeanFactory().getSingletonNames()) {
			System.out.println(beanName);
		}


		context.close();
	}
}
