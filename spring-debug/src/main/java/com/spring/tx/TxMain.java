package com.spring.tx;

import com.spring.tx.config.TxConfig;
import com.spring.tx.service.IUserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class TxMain {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(TxConfig.class);

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
	}

}
