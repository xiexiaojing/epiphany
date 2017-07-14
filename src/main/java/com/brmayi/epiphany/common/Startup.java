package com.brmayi.epiphany.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Startup {
	public static ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");//加载spring task
	public static int FULL_THREAD_TOTAL = 10;
	public static ExecutorService service = Executors.newFixedThreadPool(FULL_THREAD_TOTAL);
	public static boolean isRunning = false;
	public static final AtomicInteger threadNumber = new AtomicInteger(0);
}
