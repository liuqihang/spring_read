package com.spring.at.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.spring.at")
// 这里exposeProxy=true 表示暴露代理，核心作用是将AOP代理对象暴露到ThreadLocal中，允许AopContext.currentProxy获取代理对象，
// 该配置主要解决目标对象内部方法调用时AOP增强失效的问题
// (内部调用默认走this,而非代理，注意该配置仅对Spring AOP代理生效，对AspectJ原生织入无效)
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class AtConfig {

	@Bean
	public DataSource dataSource() {
		DruidDataSource ds = new DruidDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUrl("jdbc:mysql://localhost:3306/my_test");
		ds.setUsername("root");
		ds.setPassword("123456");
		// 可选 Druid 连接池配置
		ds.setInitialSize(2);
		ds.setMaxActive(10);
		ds.setMinIdle(1);
		ds.setMaxWait(3000);
		return ds;
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public PlatformTransactionManager transactionManager(DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}
}
