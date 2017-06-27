package com.brmayi.epiphany.business;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.strategy.ThreadDistributionStrategy;
import com.brmayi.epiphany.util.EpiphanyFileUtil;

public class FullStartupTask implements Runnable {
	private final static Logger LOGGER = LoggerFactory.getLogger(FullStartupTask.class);
	
	private String filePath;
	
	@Resource
	private ThreadDistributionStrategy strategy;
	
	private int threadCount;
	
	private Object[] objects;
	
	public FullStartupTask(String filePath, ThreadDistributionStrategy strategy, int threadCount, Object[] objects) {
		this.filePath = filePath;
		this.strategy = strategy;
		this.threadCount = threadCount;
		this.objects = objects;
	}
	
	@Override
	public void run() {
		clearData();
		EpiphanyFileUtil.createPath(filePath);
		List<FullBusinessTask> threads = strategy.getStrategy(threadCount, filePath, objects);
		ExecutorService executor = Executors.newFixedThreadPool(50);
		for(FullBusinessTask t : threads) {
			executor.execute(t);
		}
	}

    
	private void clearData() {
		 SimpleDateFormat sdfForDir = new SimpleDateFormat("yyyyMMdd");
		 Calendar now = Calendar.getInstance();
		 File dir = new File(filePath);
		 if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
               File division = new File(new StringBuffer(filePath).append("/").append(children[i]).toString());
               if(division.isDirectory()) {
            	   String[] dateDirs = division.list();
            	   for(int j=0; j<dateDirs.length; j++) {
            		   if(dateDirs[j]!=null) {
	            		   Date date = null;
						   try {
							date = sdfForDir.parse(dateDirs[j]);
							} catch (ParseException e) {
								LOGGER.error("日期解析错误", e);
							}
	        			  if(now.getTimeInMillis()-date.getTime()>2*24*3600*1000) {
	        				  deleteDir(new File(new StringBuffer(filePath).append("/").append(children[i]).append("/").append(dateDirs[j]).toString()));
	        			  }
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
