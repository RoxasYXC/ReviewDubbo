package com.yxc.review.dubbo.provider;

public class DemoServiceImpl implements DemoService {

	public String sayHello(String name) {
		return name + " hello";
	}

}
