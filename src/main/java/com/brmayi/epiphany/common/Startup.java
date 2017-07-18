package com.brmayi.epiphany.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Startup {
	public static ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");//加载spring task
	public static boolean isRunning = false;
	public static final AtomicInteger threadNumber = new AtomicInteger(0);
}
