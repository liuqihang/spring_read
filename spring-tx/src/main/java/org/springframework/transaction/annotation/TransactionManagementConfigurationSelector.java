/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.util.ClassUtils;

/**
 * Selects which implementation of {@link AbstractTransactionManagementConfiguration}
 * should be used based on the value of {@link EnableTransactionManagement#mode} on the
 * importing {@code @Configuration} class.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see EnableTransactionManagement
 * @see ProxyTransactionManagementConfiguration
 * @see TransactionManagementConfigUtils#TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME
 * @see TransactionManagementConfigUtils#JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME
 */
public class TransactionManagementConfigurationSelector extends AdviceModeImportSelector<EnableTransactionManagement> {

	/**
	 * Returns {@link ProxyTransactionManagementConfiguration} or
	 * {@code AspectJ(Jta)TransactionManagementConfiguration} for {@code PROXY}
	 * and {@code ASPECTJ} values of {@link EnableTransactionManagement#mode()},
	 * respectively.
	 */
	@Override
	protected String[] selectImports(AdviceMode adviceMode) {
		switch (adviceMode) {
			case PROXY:
				//AutoProxyRegistrar ==>
				// 		beanName:org.springframework.aop.config.internalAutoProxyCreator
				// 		类型：InfrastructureAdvisorAutoProxyCreator
				// 		作用：自动代理创建器(BPP), 决定是否给bean生成代理

				//ProxyTransactionManagementConfiguration ==>
				// 		beanClass:org.springframework.transaction.annotation.ProxyTransactionManagementConfiguration
				// 		角色: 配置类内含@Configuration注解, 注册所有事物基础设施bean
				//				==> 引入beanName:org.springframework.transaction.config.internalTransactionAdvisor
				//				==> 类型:BeanFactoryTransactionAttributeSourceAdvisor
				//				==> 角色:切点 + Advice, 决定哪些方法要织入事务
				//
				//				==> 引入beanName:transactionAttributeSource
				//				==> 类型:AnnotationTransactionAttributeSource
				//				==> 角色:解析@Transactional, 决定 propagation-传播方式、 isolation-隔离级别、rollback rules-回滚规则
				//
				//				==> 引入beanName:transactionInterceptor
				//				==> 类型:transactionInterceptor
				//				==> 角色:实际执行事务逻辑的  invokeWithinTransaction()方法

				//		其继承的父类AbstractTransactionManagementConfiguration引入
				//				==> beanName:org.springframework.transaction.config.internalTransactionalEventListenerFactory
				//				==> 类型:TransactionalEventListenerFactory
				//				==> 角色:支持@TransactionalEventListener, 让事件绑定到事务生命周期

				return new String[] {AutoProxyRegistrar.class.getName(),
						ProxyTransactionManagementConfiguration.class.getName()};
			case ASPECTJ:
				return new String[] {determineTransactionAspectClass()};
			default:
				return null;
		}
	}

	private String determineTransactionAspectClass() {
		return (ClassUtils.isPresent("javax.transaction.Transactional", getClass().getClassLoader()) ?
				TransactionManagementConfigUtils.JTA_TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME :
				TransactionManagementConfigUtils.TRANSACTION_ASPECT_CONFIGURATION_CLASS_NAME);
	}

}
