package com.spring.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpService {

	@Transactional
	public void createUser() {
		System.out.println(">>> enter createUser");
		inner();
		System.out.println(">>> exit createUser");
	}

	@Transactional
	public void inner() {
		System.out.println(">>> enter inner");
	}
}
