package com.spring.test;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

	private int count;
	private String mapperName;

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getMapperName() {
		return this.mapperName;
	}

	public void setMapperName(String name) {
		this.mapperName = mapperName;
	}
}
