package com.brmayi.epiphany.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
/**
 * 
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
public class TimerUtil {
	 private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;
	 private static final Timer timer = new Timer();
	 public static void runEveryday(TimerTask task, int hour, int minute, int second) {
		 Calendar calendar = Calendar.getInstance();
		 calendar.set(Calendar.HOUR_OF_DAY, hour);
		 calendar.set(Calendar.MINUTE, minute);
		 calendar.set(Calendar.SECOND, second);
		 Date date=calendar.getTime(); //第一次执行定时任务的时间
		 if (date.before(new Date())) {
			 date = addDay(date, 1);
		 }
		 timer.schedule(task,date,PERIOD_DAY);
	 }
	 
	 // 增加或减少天数
	 public static Date addDay(Date date, int num) {
		Calendar startDT = Calendar.getInstance();
		startDT.setTime(date);
		startDT.add(Calendar.DAY_OF_MONTH, num);
		return startDT.getTime();
	 }
}
