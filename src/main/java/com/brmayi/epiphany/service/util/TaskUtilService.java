package com.brmayi.epiphany.service.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Deflater;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;


/**
 * 
 * **************************************************************
 * @ClassName: TaskUtilService 
 * @Description: 非静态工具service，避免并发问题
 * @author <a href='mailto:987489055@qq.com'>Jane Xie</a>
 * @date 2016年8月8日 下午3:11:46
 *  
 * **************************************************************
 */
@Component("taskUtilService")
public class TaskUtilService {
	private final static Logger LOGGER = LoggerFactory.getLogger(TaskUtilService.class); 
    
    @Resource
	private JmsTemplate jmsTemplate;
	
    @Resource
	private Destination videoDestination;
    
    @Resource
	private Destination albumDestination;
    
    @Value("${mms.search.transmission.path}")
    private String filePath;
    
    public static final AtomicInteger videoThreadNumber = new AtomicInteger(0);
    public static final AtomicInteger albumThreadNumber = new AtomicInteger(0);
    public static final AtomicInteger audioThreadNumber = new AtomicInteger(0);
    public static final AtomicInteger audioAlbumThreadNumber = new AtomicInteger(0);

    public static final int AUDIO_LIMIT_DEAL_ONE_TIME = 200;
    public static final int AUDIO_ALBUM_LIMIT_DEAL_ONE_TIME = 200;
    public static final int ALBUM_LIMIT_DEAL_ONE_TIME = 150;
    public static final int VIDEO_LIMIT_DEAL_ONE_TIME = 800;
    public static final int MEMCAHE_EXPIRE_TIME = 24*3600;
    
    public static final int ALBUM_VIDEO_LIMIT_DEAL_ONE_TIME = 1000;
    
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static ApplicationContext context = null;
    
    public static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(80); 
	
    public static final ExecutorService fixedThreadPoolForGz = Executors.newFixedThreadPool(6);
	public static final int ALBUM_RUN_INTERVAL = 60;
	public static final int VIDEO_RUN_INTERVAL = 60;
	
	public static final int ALBUM_RUN_END_TIME = -3;
	public static final int VIDEO_RUN_END_TIME = -4;

	/**
	 * 创建磁盘目录
	 * @param division 目录区分
	 * @return
	 */
	public String createPath(String division) {
		StringBuffer path = new StringBuffer(filePath).append(division).append("/").append(getDate(null, 0));
	    File f = new File(path.toString());
	    if(f.exists()) {
	    	String[] children = f.list();
            for(String c : children) {
            	new File(f, c).delete();
            }
	    } else {
	    	f.mkdirs();
	    }
	    return path.append("/").append(division).toString();
    }

	public StringBuffer getPath(String division) {
	    return new StringBuffer(filePath).append(division).append("/").append(getDate(null, 0)).append("/").append(division);
    }

	private static String getDate(Integer timeUnit, int interval) {
		SimpleDateFormat sdfForDir = new SimpleDateFormat("yyyyMMdd");
		Calendar date = Calendar.getInstance();
		if(timeUnit!=null) {
			date.set(timeUnit, date.get(timeUnit)+interval);
		}
		return sdfForDir.format(date.getTime());
	}
	
    /**
     * 备份上次文件
     * @param path 文件全路径
     */
	public void renameFile(String path) {
	    File f = new File(path);
	    if(f.exists()) {
	    	f.renameTo(new File(new StringBuffer(path).append("_bak_").append(System.currentTimeMillis()).toString()));
	    }
    }
    
    /**
     * 将json数据写入磁盘文件，分批写释放内存
     * @param path 文件全路径
     * @param content 写入内容
     */
	public void writeToFile(String path, String content) {
    	try (RandomAccessFile randomFile = new RandomAccessFile(path, "rw");){// 打开一个随机访问文件流，按读写方式
            // 文件长度，字节数
            long fileLength = randomFile.length();
            //将写文件指针移到文件尾。
            randomFile.seek(fileLength);
            randomFile.write(content.getBytes("UTF8"));
        } catch (IOException e) {
        	LOGGER.error("写入全量专辑文件失败", e);
        }
    }
    
    /**
     * 通过SwiftQ传输
     * @param content 传输内容
     */
	public void swiftSendMsg(final String content, boolean needDelay) {
		try {
			Destination transDestination;
			if(needDelay) {
				jmsTemplate.setReceiveTimeout(10000);
				transDestination = albumDestination;
			} else {
				transDestination = videoDestination;
			}
	        jmsTemplate.send(transDestination, new MessageCreator() {
	            public Message createMessage(Session session)throws JMSException {
	            	TextMessage msg = null;
					try {
						msg = session.createTextMessage(content);
					} catch (Exception e) {
						LOGGER.error("消息队列"+content, e);
					}
	                return msg;
	            }
	        });
		} catch(Exception e) {
			LOGGER.error("send msg error, size:{}, data:{}",content.length(), content.substring(0, Math.min(content.length()-1, 80)),e);
		}
    }
	  
    /** 
     * 压缩 
     *  
     * @param data 
     *            待压缩数据 
     * @return byte[] 压缩后的数据 
     */  
    public byte[] compress(byte[] data) {
        byte[] output = new byte[0];  
  
        Deflater compresser = new Deflater();  
        compresser.reset();  
        compresser.setInput(data);  
        compresser.finish();  
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length)){  
            byte[] buf = new byte[data.length];  
            while (!compresser.finished()) {  
                int i = compresser.deflate(buf);  
                bos.write(buf, 0, i);  
            }  
            output = bos.toByteArray();  
        } catch (Exception e) {  
            output = data;  
            LOGGER.error("队列消息压缩错误", e);
        }
        compresser.end();  
        return output;  
    }
    
    /**
     * 取当前时间
     * @return 当前时间
     */
	public static String getNewTime(Integer timeUnit, int interval) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    	Calendar date = Calendar.getInstance();
		if(timeUnit!=null) {
			date.set(timeUnit, date.get(timeUnit)+interval);
		}
    	String newTime = sdf.format(date.getTime());
    	return newTime;
    }
	
	/**
	 * 时间格式转换
	 * 
	 * @param lastUpdateTime
	 * @return
	 */
	public static String getLastUpdateTime(Date lastUpdateTime) {
		if(lastUpdateTime != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			return sdf.format(lastUpdateTime);
		}
		return null;
    }
	
	/**
	 * 获取前N分钟
	 * 
	 * @param minute
	 * @return
	 */
    public static String getTimeByMinute(int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minute);
        return new SimpleDateFormat(DATE_FORMAT).format(calendar.getTime());
    }
}
