package com.spring.at.service.impl;

import com.spring.at.service.ILogService;
import org.springframework.stereotype.Service;

@Service
public class LogService implements ILogService {

	// final会让方法无法增强进行代理
//	public void printLog(){
//		System.out.println("start print log");
//	}

	public String auth(Long userId){
		System.out.println("start print auth, userId:" + userId);
		return "auth success";
	}
}
