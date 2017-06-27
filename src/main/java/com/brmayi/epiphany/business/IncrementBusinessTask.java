package com.brmayi.epiphany.business;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import net.rubyeye.xmemcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.service.DataService;
import com.brmayi.epiphany.service.util.TaskUtilService;

public class IncrementBusinessTask implements Runnable{
	private static final Logger logger = LoggerFactory.getLogger(IncrementBusinessTask.class);
	public static final int MEMCAHE_EXPIRE_TIME = 24*3600;
	public static final int RUN_END_TIME = -1;
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
				lastTime = TaskUtilService.getNewTime(Calendar.SECOND,-2*TaskUtilService.ALBUM_RUN_INTERVAL+TaskUtilService.ALBUM_RUN_END_TIME);
			} else {
				logger.info("lastTime{}", lastTime);
			}
			String endTime = TaskUtilService.getNewTime(Calendar.SECOND, RUN_END_TIME); //当前时间
			memcacheClient.set(cacheKey, MEMCAHE_EXPIRE_TIME, endTime);
			List<Long> ids = dataService.getIds(lastTime, endTime);
			dataService.dealData(ids);
			ids = null;
		} catch(Exception e) {
			throw new EpiphanyException(e);
		}
	}
	
}
