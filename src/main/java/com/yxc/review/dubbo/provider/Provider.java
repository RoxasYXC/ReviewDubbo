package com.yxc.review.dubbo.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Provider {
	public static void main(String[] args) throws Exception {
//        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:conf/provider.xml");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:conf/provider_zk.xml");
        context.start();
        System.in.read(); // 按任意键退出
    }
}
