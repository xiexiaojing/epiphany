/**
 * 
 */
package com.brmayi.epiphany.business;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.brmayi.epiphany.common.Startup;
import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.service.DataService;
import com.brmayi.epiphany.util.EpiphanyFileUtil;
import com.brmayi.epiphany.util.GzCompressUtil;

/**
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
public class FullBusinessTask implements Runnable {
	private final static Logger LOGGER = LoggerFactory.getLogger(FullBusinessTask.class);
	protected static final AtomicInteger threadNumber = new AtomicInteger(0);
	private static final String EMPTY = "";

	private int threadNo=0;
	private String fullPath="/data/epiphany";
    @Resource
    private RedisTemplate<String, Long> redisTemplate;
	private DataService dataService=null;
	private long minId=0;
	private long maxId=20000;
	private int dealOneTime = 200;
	private String redisNoKey = "epiphanyNo";

	public void setMinId(long minId) {
		this.minId = minId;
	}
	
	public void setMaxId(long maxId) {
		this.maxId = maxId;
	}

	public void setThreadNo(int threadNo) {
		this.threadNo = threadNo;
	}
	
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setDealOneTime(int dealOneTime) {
		this.dealOneTime = dealOneTime;
	}

	public void setRedisNoKey(String redisNoKey) {
		this.redisNoKey = redisNoKey;
	}

	@Override
	public void run() throws EpiphanyException {
		threadNumber.incrementAndGet();
		StringBuilder pathBuilder = EpiphanyFileUtil.getPath(fullPath);
		String path = pathBuilder.append(threadNo).toString();
		long endThisTime = redisTemplate.opsForValue().increment(redisNoKey, dealOneTime);
		long curId = minId+endThisTime-dealOneTime;
		while (curId <= maxId) {
			StopWatch stopWatch = new Slf4JStopWatch(fullPath);
			dataService.dealDataByBeginEnd(curId, minId+endThisTime, path);
			stopWatch.stop();
			endThisTime = redisTemplate.opsForValue().increment(redisNoKey, dealOneTime);
			curId = endThisTime-dealOneTime;
		}
		
		GzCompressUtil.gzCompress(path);//压缩
		threadNumber.decrementAndGet();
		if(threadNumber.get()==0) {
			LOGGER.info("fullExecute generate success");
			EpiphanyFileUtil.writeToFile(pathBuilder.append("success").toString(), EMPTY);
			Startup.isRunning =false;
		}
	}
}
