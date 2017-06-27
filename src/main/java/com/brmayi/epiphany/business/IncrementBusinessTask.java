package com.brmayi.epiphany.business;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import net.rubyeye.xmemcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.service.DataService;

public class IncrementBusinessTask implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(IncrementBusinessTask.class);
	public static final int MEMCAHE_EXPIRE_TIME = 24*3600;
	public static final int RUN_END_TIME = -1;
	public static final int RUN_INTERVAL = -1;
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
    @Resource
    private MemcachedClient memcacheClient;
    
    @Resource
    private DataService dataService;
    
    private String cacheKey;

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	@Override
	public void run() throws EpiphanyException{
		try {
			String lastTime = memcacheClient.get(cacheKey);
			if(StringUtils.isEmpty(lastTime)) {
				lastTime = getNewTime(Calendar.SECOND,-2*RUN_INTERVAL+RUN_END_TIME);
			} else {
				logger.info("lastTime{}", lastTime);
			}
			String endTime = getNewTime(Calendar.SECOND, RUN_END_TIME); //当前时间
			memcacheClient.set(cacheKey, MEMCAHE_EXPIRE_TIME, endTime);
			List<Long> ids = dataService.getIds(lastTime, endTime);
			dataService.dealData(ids);
			ids = null;
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
