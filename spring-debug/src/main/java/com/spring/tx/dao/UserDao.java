package com.spring.tx.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public int queryBalance(Long userId) {
		return jdbcTemplate.queryForObject(
				"select balance from user where id = ?",
				Integer.class,
				userId
		);
	}

	public void updateBalance(Long userId, int newBalance) {
		jdbcTemplate.update(
				"update user set balance = ? where id = ?",
				newBalance, userId
		);
	}
}
