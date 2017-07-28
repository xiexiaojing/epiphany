package com.brmayi.epiphany.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
/**
 * 	 包含各种启动选项：包括加载spring上下文，是客户端需要直接调用的一个类
 * 
 *            
 *            .==.       .==.
 *           //'^\\     //^'\\
 *          // ^^\(\__/)/^ ^^\\
 *         //^ ^^ ^/6  6\ ^^^ \\
 *        //^ ^^ ^/( .. )\^ ^^ \\
 *       // ^^  ^/\|v""v|/\^^ ^ \\
 *      // ^^/\/  / '~~' \ \/\^ ^\\
 *      ----------------------------------------
 *      HERE BE DRAGONS WHICH CAN CREATE MIRACLE
 *       
 *      @author 静儿(987489055@qq.com)
 *
 */
public class Startup {
	/**
	 * spring 上下文：路径请放于classpath的spring文件夹下
	 */
	public static ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");//加载spring task
	/**
	 * 当前运行中的全量线程计数器
	 */
	public static final AtomicInteger threadNumber = new AtomicInteger(0);
}
