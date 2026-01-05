package com.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService{

//	@Autowired
//	private UserRepository userRepository;

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository){
		this.userRepository = userRepository;
	}

	@Override
	public void sayHello() {
		System.out.println("say hello");
		userRepository.selectById();
	}
}
