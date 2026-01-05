package com.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserRepository {

//	@Autowired
//	private UserService userService;

	private final UserService userService;

	public UserRepository(UserService userService){
		this.userService = userService;
	}

	void selectById(){
		System.out.println("select 1 from user where id=1");
	}
}
