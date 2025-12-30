package com.spring.test.bfpp;

import com.spring.test.Person;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {


	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("执行 MyBeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry ... ");
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Person.class);
		builder.addPropertyValue("name", "liuqihang");
		registry.registerBeanDefinition("person", builder.getBeanDefinition());
	}
}
