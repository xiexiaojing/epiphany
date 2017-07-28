/**
 * 
 */
package com.brmayi.epiphany.business;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
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
public class NiceQueueTask implements Runnable {
	private final static Logger LOGGER = LoggerFactory.getLogger(NiceQueueTask.class);
	
	private static final String EMPTY = "";

	private int threadNo=0;
	private String fullPath="/data/epiphany";
    @Resource
    private RedisTemplate<String, Long> redisTemplate;
	private DataService dataService=null;
	private String redisNoKey = "epiphanyNo";

	public void setThreadNo(int threadNo) {
		this.threadNo = threadNo;
	}
	
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	public void setRedisNoKey(String redisNoKey) {
		this.redisNoKey = redisNoKey;
	}

	@Override
	public void run() throws EpiphanyException {
		Startup.threadNumber.incrementAndGet();
		String pathWithDate = EpiphanyFileUtil.getPath(fullPath);
		String path = new StringBuilder(pathWithDate).append(threadNo).toString();
		String numKey = new StringBuilder("n").append(redisNoKey).toString();
		String reverseListKey = new StringBuilder("r").append(redisNoKey).toString();
		while(true) {
			long endThisTime = redisTemplate.opsForValue().increment(numKey, 1);
			StopWatch stopWatch = new Slf4JStopWatch(fullPath);
			Object id = redisTemplate.opsForHash().get(reverseListKey, String.valueOf(endThisTime));
			if(id==null) {
				break;
			}
			List<Long> ids = new ArrayList<Long>();
			ids.add(NumberUtils.toLong((String) id));
			dataService.dealDataByIds(ids, path);
			stopWatch.stop();
		}
		GzCompressUtil.compressFile(path);//压缩
		Startup.threadNumber.decrementAndGet();
		if(Startup.threadNumber.get()==0) {
			LOGGER.info("fullExecute generate success");
			String division = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.length());
			EpiphanyFileUtil.writeToFile(new StringBuilder(pathWithDate).append(division).append("success").toString(), EMPTY);
			redisTemplate.delete(numKey);
			redisTemplate.delete(reverseListKey);
		}
	}
}
