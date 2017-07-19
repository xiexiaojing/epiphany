package com.brmayi.epiphany.business;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.brmayi.epiphany.common.Startup;
import com.brmayi.epiphany.service.DataService;
import com.brmayi.epiphany.util.EpiphanyFileUtil;

public class FullStartupTask  extends TimerTask {
	private final static Logger LOGGER = LoggerFactory.getLogger(FullStartupTask.class);
	public int threadTotal = 23;
	public ExecutorService service = Executors.newFixedThreadPool(threadTotal);
	
    @Resource
    private RedisTemplate<String, String> redisTemplate;
	private String fullPath="/data/epiphany";
    private DataService dataService=null;
	private int dealOneTime = 200;
	private String redisNoKey = "epiphanyNo";

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

	public void setThreadTotal(int threadTotal) {
		this.threadTotal = threadTotal;
		service = Executors.newFixedThreadPool(threadTotal);
	}

	@Override
	public void run() {
		redisTemplate.opsForValue().set(redisNoKey, "0");
		clearData();
		EpiphanyFileUtil.createPath(fullPath);
		ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
		String maxKey = new StringBuilder("max").append(redisNoKey).toString();
    	String maxId = valueOperations.get(maxKey);
    	if(maxId==null) {
    		maxId = String.valueOf(dataService.getMaxId());
    		valueOperations.set(maxKey, maxId);
    	}
    	String minKey = new StringBuilder("min").append(redisNoKey).toString();
    	String minId = valueOperations.get(minKey);
    	if(minId==null) {
    		minId = String.valueOf(dataService.getMinId());
    		valueOperations.set(minKey, minId);
    	}
    	Startup.isRunning=true;
    	List<Long> niceQueue = dataService.getNiceQueue();
    	boolean hasPriority = false;
    	if(niceQueue!=null && !niceQueue.isEmpty()) {
    		String numKey = new StringBuilder("n").append(redisNoKey).toString();
    		redisTemplate.opsForValue().set(numKey, "0");
    		String reverseListKey = new StringBuilder("r").append(redisNoKey).toString();
    		redisTemplate.delete(reverseListKey);
    		String prioritySetKey = new StringBuilder("ps").append(redisNoKey).toString();
    		redisTemplate.delete(prioritySetKey);
    		for(int i=1; i<=niceQueue.size(); i++) {
    			String idstr = String.valueOf(niceQueue.get(i-1));
    			redisTemplate.opsForHash().put(reverseListKey, String.valueOf(i), idstr);
    			redisTemplate.opsForSet().add(prioritySetKey, idstr);
    		}
        	for(int i=0; i<threadTotal; i++) {
        		NiceQueueTask niceQueueTask = (NiceQueueTask) Startup.context.getBean("niceQueueTask");
        		niceQueueTask.setThreadNo(i);
        		niceQueueTask.setFullPath(fullPath);
        		niceQueueTask.setDataService(dataService);
        		niceQueueTask.setRedisNoKey(redisNoKey);
        		service.execute(niceQueueTask);
            }
        	hasPriority = true;
    	}
    	for(int i=0; i<threadTotal; i++) {
    		FullBusinessTask fullBusinessTask = (FullBusinessTask) Startup.context.getBean("fullBusinessTask");
    		fullBusinessTask.setMinId(NumberUtils.toLong(minId));
    		fullBusinessTask.setMaxId(NumberUtils.toLong(maxId));
    		fullBusinessTask.setThreadNo(i);
    		fullBusinessTask.setFullPath(fullPath);
    		fullBusinessTask.setDataService(dataService);
    		fullBusinessTask.setDealOneTime(dealOneTime);
    		fullBusinessTask.setRedisNoKey(redisNoKey);
    		fullBusinessTask.setHasPriority(hasPriority);
    		service.execute(fullBusinessTask);
        }
	}

	private void clearData() {
		 SimpleDateFormat sdfForDir = new SimpleDateFormat("yyyyMMdd");
		 Calendar now = Calendar.getInstance();
		 File dir = new File(fullPath);
		 if (dir.isDirectory()) {
            //递归删除目录中的子目录下
               if(dir.isDirectory()) {
            	   String[] dateDirs = dir.list();
            	   for(int j=0; j<dateDirs.length; j++) {
            		   if(dateDirs[j]!=null) {
	            		   Date date = null;
						   try {
							date = sdfForDir.parse(dateDirs[j]);
							} catch (ParseException e) {
								LOGGER.error("日期解析错误", e);
							}
	        			  if(now.getTimeInMillis()-date.getTime()>2*24*3600*1000) {
	        				  deleteDir(new File(new StringBuffer(fullPath).append("/").append(dateDirs[j]).toString()));
	        			  }
            		   }
            	   }
               }
        }
	 }
	 
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        boolean isDeleted = dir.delete();
        LOGGER.info("删除目录"+dir.getName()+(isDeleted?"成功":"失败"));
        return isDeleted;
    }
}
