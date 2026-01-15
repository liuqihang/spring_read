package com.spring.aop;

import org.springframework.stereotype.Service;

@Service
public class LogService implements ILogService{

	// final会让方法无法增强进行代理
	public void printLog(){
		System.out.println("start print log");
	}

	public void auth(){
		System.out.println("start print auth");
	}
}
