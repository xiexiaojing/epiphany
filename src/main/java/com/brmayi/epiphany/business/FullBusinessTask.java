/**
 * 
 */
package com.brmayi.epiphany.business;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final int DEAK_ONE_TIME = 100;
	private static final String EMPTY = "";
	private List<Long> ids;
	private int threadNo;
	private String fullPath;
	
	@Resource
	private DataService dataService;
	
	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public void setThreadNo(int threadNo) {
		this.threadNo = threadNo;
	}
	
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	@Override
	public void run() throws EpiphanyException {
		threadNumber.incrementAndGet();
		StringBuilder pathBuilder = EpiphanyFileUtil.getPath(fullPath);
		String path = pathBuilder.append(threadNo).toString();
		int count = ids.size();
		int curId = 0;
		while(curId<count) {
			long beginTime = System.currentTimeMillis();
			int endThisTime = curId+DEAK_ONE_TIME;
			if(endThisTime>count) {
				endThisTime=count;
			}
			LOGGER.info("threadNo:{},begin:{},end:{},left undeal:{}, begin", threadNo, curId, count, count-curId);
			dataService.dealData(ids);
			LOGGER.info("threadNo:{}, use ms:{}, thread count in progress:{}", threadNo, (System.currentTimeMillis()-beginTime), threadNumber.get());
			curId = endThisTime;
		}
		GzCompressUtil.gzCompress(path);//压缩
		threadNumber.decrementAndGet();
		if(threadNumber.get()==0) {
			LOGGER.info("fullExecute generate success");
			EpiphanyFileUtil.writeToFile(pathBuilder.append("success").toString(), EMPTY);
		}
	}
	
}
