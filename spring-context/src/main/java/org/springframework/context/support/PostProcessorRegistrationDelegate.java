/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	private PostProcessorRegistrationDelegate() {
	}

	//让所有 BeanFactoryPostProcessor 按规则、有序、完整地作用于 BeanFactory / BeanDefinition，确保 “Bean 的定义体系” 在 Bean 创建前被完全准备好。
	//这里的顺序：PriorityOrdered → Ordered → 无序
	//区分 BeanDefinitionRegistryPostProcessor 和 BeanFactoryPostProcessor
	//  最先执行BeanDefinitionRegistryPostProcessor，来进行增加/修改 BeanDefinition，必须最早执行，因为影响后续扫描、配置
	//	BeanFactoryPostProcessor修改 BeanFactory 属性,必须在BeanDefinition 完整之后，所以排在执行BeanDefinitionRegistryPostProcessor之后
	/*
	 * 顺序	目标														示例
	 * 1	BeanDefinitionRegistryPostProcessor + PriorityOrdered	ConfigurationClassPostProcessor
	 * 2	BeanDefinitionRegistryPostProcessor + Ordered			自定义扫描器
	 * 3	BeanDefinitionRegistryPostProcessor + 无序				其他注册增强
	 * 4	BeanFactoryPostProcessor + PriorityOrdered				早期调整工厂属性
	 * 5	BeanFactoryPostProcessor + Ordered						调整 Factory 的行为
	 * 6	BeanFactoryPostProcessor + 无序							剩余的
	 */
	/*
		为什么必须这样设计？（本质原因）
				设计点									目的							如果不这样做会怎样
		先 Registry，再 Factory				Bean 定义必须完整后才能改工厂属性			Factory 修改可能基于不完整定义
		多轮循环执行 Registry					允许后生成的定义继续参与					新 BeanDefinition 无法处理
		PriorityOrdered > Ordered > 无序		可控、可声明顺序						执行顺序不稳定，影响框架扩展
		用户传入 > 容器内部					用户至上								框架强压用户逻辑，扩展性差
		排序 & 分类 & 批次处理					可插拔、可扩展、可维护					模块间互相覆盖、执行顺序不可预期
	 */
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		/**
		 * beanFactory == public class DefaultListableBeanFactory implements ... BeanDefinitionRegistry ...
		 * 所以这里的if符合条件
		 */
		//这一层的beanFactoryPostProcessors参数都是手动调用addBeanFactoryPostProcessor注册进来的(自定义实现BeanFactoryPostProcessor然后手动add)，一般是空list
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				//如果是BeanDefinitionRegistryPostProcessor则添加到registryProcessors
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor =
							(BeanDefinitionRegistryPostProcessor) postProcessor;
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				}
				else {
					//反之添加到regularPostProcessors
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.

			//保存本次要执行的BeanDefinitionRegistryPostProcessor
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			//按照优先级进行排序操作
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			//添加到registryProcessors，用于最后执行postProcessBeanFactory方法
			registryProcessors.addAll(currentRegistryProcessors);
			//遍历currentRegistryProcessors，执行postProcessBeanDefinitionRegistry方法
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			//执行完毕后清空
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			//这部分的实现和上面的代码逻辑一致，因为上面的invokeBeanDefinitionRegistryPostProcessors执行过程中可能会新增其他的BeanDefinitionRegistryPostProcessor
			/**
			 * 因为 执行一个 RegistryPostProcessor 可能会产出新的 BeanDefinition、甚至新的 RegistryPostProcessor：
			 * ConfigurationClassPostProcessor 扫描 @Configuration → 发现更多 @Bean
			 * ComponentScan → 发现新类 → 可能包含新处理器
			 * 如果不循环，就会漏掉新产生的处理器 / Bean 定义。
			 * 🔑 循环 = 保证 “动态扩展的处理器” 也能参与全流程。
			 */
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			/* （最后一次循环“while”,处理直到没有新增的Processor） */
			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		}

		else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}
		//到此上面的处理逻辑代码，已将入参传入的beanFactoryPostProcessors 和 容器中所有的BeanDefinitionRegistryPostProcessor已经全部处理完毕。
		// 下面开始处理容器中的BeanFactoryPostProcessor，因为可能有些实现类直接实现的是BeanFactoryPostProcessor，未走实现子类BeanDefinitionRegistryPostProcessor

		//这里需要和上述逻辑对比，不难注意到BeanFactoryPostProcessor未进行重复处理，只执行了一轮。因为实现BeanFactoryPostProcessor是不会再新增新的 Processor（时机太晚）的操作的(postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory))

		//网上找到一些总结：
		/*
		 	1. BeanDefinition 已加载，但 Bean 还没创建
			2. 执行 BeanDefinitionRegistryPostProcessor（可以新增/修改 BeanDefinition）
			3. BeanDefinition 可能被新增 → 需要重新扫描确保新注册的也能执行
			4. 所有 BeanDefinition 稳定
			5. 执行 BeanFactoryPostProcessor（只能修改已有的 BeanDefinition / BeanFactory）

			职责：
				BeanDefinitionRegistryPostProcessor 的职责，可以注册新的 BeanDefinition，（不仅是普通 Bean，还可能是其他 Processor）
				伪代码：
					class MyRegistryProcessor implements BeanDefinitionRegistryPostProcessor {
						public void postProcessBeanDefinitionRegistry(registry) {
							// 动态注册一个新的后处理器
							registry.registerBeanDefinition("xxx", new BeanDefinition(MyCustomProcessor.class));
						}
					}
				在执行第一个 RegistryPostProcessor 时：发现注册了新的 Processor
				所以必须 再次扫描 BeanDefinition 以便执行它，否则它永远不会被执行
				👉 这就是为什么 RegistryPostProcessor 执行是循环的
				Spring 的逻辑：
				🔁 扫描 → 执行 → 看是否新增 → 若新增则继续
				直到 所有 RegistryPostProcessor 都被执行过。


				BeanFactoryPostProcessor的职责是对现有的BeanFactory、BeanDefinition进行修改，而不是新增定义。
				设计上要求它 不应该再注册新的 Processor，否则：执行顺序将变得不可预测
				BeanFactory 已进入中后期加工，影响面大、复杂性高
				Spring 希望扩展点集中在 Registry 阶段，保证 BeanDefinition 定型后再做 BeanFactory 级别加工
		 */

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them! 翻译：不要在这里初始化FactoryBeans：我们需要保留所有常规Bean未初始化，以便让Bean工厂的后处理器对其应用！
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		//这里ordered和non使用String来存储, 而priority用BFPP来存储是Spring特意设计的，为了延迟实例化getBean, 分级排序。主要是PriorityOrdered依然可能修改 BeanDefinition、注册新的 BeanDefinition、改变属性值、scope、lazy。
		//Ordered的排序依赖是需要可以调用到getOrder()方法，而能调用方法是建立在bean已经实例化的基础上的, 然后现在分类阶段还在BFPP可以调整beanDefinition的阶段，这个阶段不合适提前getBean Ordered类的BFPP

		//   -------------------------------------分类阶段	START -------------------------------------------------------------------------------
		/*
			PriorityOrdered 的 BFPP 在分类阶段即允许实例化：
			 1. 其语义要求最早执行
			 2. 通常用于影响容器结构（BeanDefinition 注册 / 修改）
			 3. Spring 认为其实例化副作用是“可接受且必要的”
		 */
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();

		/*
			Ordered / NonOrdered 在分类阶段仅记录 beanName，而不实例化：
			1. 避免在 BFPP 分类阶段提前触发 getBean()
			2. 防止 Ordered BFPP 的构造器 / 注入逻辑在 PriorityOrdered BFPP 尚未执行前生效
			3. 确保所有 PriorityOrdered BFPP 的BeanDefinition 修改已完成后，再实例化其他 BFPP
		*/
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			}
			else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}
		//   -------------------------------------分类阶段	END  -------------------------------------------------------------------------------


		//   -------------------------------------执行阶段	START  -----------------------------------------------------------------------------
		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		//   -------------------------------------执行阶段	END  -------------------------------------------------------------------------------

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		beanFactory.clearMetadataCache();
	}

	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.

		// 这里记录count , 为什么+1，因为在此方法添加一个 BeanPostProcessorChecker 类
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
		List<String> orderedPostProcessorNames = new ArrayList<>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		// 前面add了一次（prepareBeanFactory），这里再add一次是为了确保最后还能执行一次。Spring做了幂等处理（内部使用 Set），所以执行两次不会有问题
		// private final Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<>();(幂等实现关键是用这个数据结构存放Listener)
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		// Nothing to sort?
		if (postProcessors.size() <= 1) {
			return;
		}
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {

		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
