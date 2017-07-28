package com.brmayi.epiphany.business;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.brmayi.epiphany.common.Startup;
import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.service.DataService;
/**
 * 
 * 	增量执行逻辑类
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
public class IncrementBusinessTask implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(IncrementBusinessTask.class);
	public static final int MEMCAHE_EXPIRE_TIME = 24*3600;
	public static final int RUN_END_TIME = -1;
	public static final int RUN_INTERVAL = -1;
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
  
	@Resource
    private RedisTemplate<String,String> redisTemplate;

    private DataService dataService;
    
    private String timeCacheKey="timeCacheKey";
	
    private String fullIncModel = "yield";
	
    /**
     * 缓存当前增量已执行过数据的时间戳key
     * @param timeCacheKey
     */
	public void setTimeCacheKey(String timeCacheKey) {
		this.timeCacheKey = timeCacheKey;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	/**
	 * 全量和增量的运行模式：
	 * 	both 共同执行模式
	 *  yield 在执行全量时停止增量的运行，在全量执行完后接断点继续执行
	 * @param fullIncModel 全量和增量的运行模式：both 共同执行模式， yield 在执行全量时停止增量的运行，在全量执行完后接断点继续执行,默认yield
	 */
	public void setFullIncModel(String fullIncModel) {
		this.fullIncModel = fullIncModel;
	}
	
	@Override
	public void run() throws EpiphanyException{
		if("yield".equals(fullIncModel) && Startup.threadNumber.get()>0) {
			return;
		}
		try {
			String lastTime = redisTemplate.opsForValue().get(timeCacheKey);
			if(StringUtils.isEmpty(lastTime)) {
				lastTime = getNewTime(Calendar.SECOND,-2*RUN_INTERVAL+RUN_END_TIME);
			} else {
				logger.info("lastTime{}", lastTime);
			}
			String endTime = getNewTime(Calendar.SECOND, RUN_END_TIME); //当前时间
			redisTemplate.opsForValue().set(timeCacheKey, endTime);
			List<Long> ids = dataService.getIds(lastTime, endTime);
			dataService.dealDataByIds(ids, null);
			ids = null;
	        //更新时间戳
			lastTime = endTime;
		} catch(Exception e) {
			throw new EpiphanyException(e);
		}
	}
    
    /**
     * 取当前时间
     * @return 当前时间
     */
	private static String getNewTime(Integer timeUnit, int interval) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    	Calendar date = Calendar.getInstance();
		if(timeUnit!=null) {
			date.set(timeUnit, date.get(timeUnit)+interval);
		}
    	String newTime = sdf.format(date.getTime());
    	return newTime;
    }
}
