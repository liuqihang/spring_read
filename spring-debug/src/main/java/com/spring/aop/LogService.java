package com.spring.aop;

import org.springframework.stereotype.Service;

@Service
public class LogService {

	public void printLog(){
		System.out.println("start print log");
	}
}
