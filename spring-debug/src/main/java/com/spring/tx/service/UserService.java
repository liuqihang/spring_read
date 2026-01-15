package com.spring.tx.service;

import com.spring.tx.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements IUserService{

	@Autowired
	private UserDao userDao;

	@Override
	@Transactional
	public void transfer(Long userId, int amount) {
		int balance = userDao.queryBalance(userId);
		System.out.println("before :" + balance);
		userDao.updateBalance(userId, balance - amount);
		balance = userDao.queryBalance(userId);
		System.out.println("after :" + balance);
		// 🔥 强制异常，测试回滚
		if (amount > 50) {
			throw new RuntimeException("simulated exception ------------");
		}
	}
}
