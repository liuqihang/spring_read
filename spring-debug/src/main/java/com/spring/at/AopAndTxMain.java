package com.spring.at;

import com.spring.at.config.AtConfig;
import com.spring.at.service.ILogService;
import com.spring.at.service.IUserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class AopAndTxMain {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(AtConfig.class);

		// ⚠️ 一定按接口拿（JDK 代理）
		IUserService userService = ctx.getBean(IUserService.class);
		System.out.println(userService);

		try {
			userService.transfer(1L, 80);
		} catch (Exception e) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!happens error:" + e.getMessage());
		}

		JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
		Integer balance = jdbcTemplate.queryForObject(
				"select balance from user where id = 1",
				Integer.class
		);
		System.out.println(" ===========  final balance:" + balance);


		ILogService logService = (ILogService) ctx.getBean("logService");
//		ILogService logService = (ILogService) ctx.getBean(LogService.class);
		System.out.println("logService Bean class = " + logService.getClass());


		logService.printLog();
		logService.auth();
	}

}
