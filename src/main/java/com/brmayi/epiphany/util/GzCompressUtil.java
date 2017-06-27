package com.brmayi.epiphany.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.pool.UnlimitedKeyedPoolableObjectFactory;

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
 *      这里单独将压缩提出来是因为压缩计算量特别大，非常占用CPU，所以需要限制线程数
 *      建议将大文件写入磁盘，这时候速度和压缩比，后者更重要，所以采用gzip而没有支持deflate实现
 *      @author 静儿(987489055@qq.com)
 *
 */
public class GzCompressUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(GzCompressUtil.class);
	private static final ExecutorService fixedThreadPoolForGz = Executors.newFixedThreadPool(2);
    
    public static void gzCompress(String path) throws EpiphanyException {
		try {
	    	GzCompress gzCompressTask = (GzCompress) UnlimitedKeyedPoolableObjectFactory.objectPool.borrowObject(GzCompress.class.getName());
		    gzCompressTask.setPath(path);
		    fixedThreadPoolForGz.execute(gzCompressTask);//压缩
		} catch(Exception e) {
			LOGGER.error("gzcompress exception", e);
			throw new EpiphanyException(e);
		}
    }
    
    private class GzCompress implements Runnable {
    	private String path;
    	
        public void setPath(String path) {
    		this.path = path;
    	}
        
		/**
	     * hadoop支持gz格式自解压，gz可以压缩到原文本文件的1/7
	     * 
	     * @param path 压缩的文件路径
	     */
		@Override
		public void run() {
	       	try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
	    		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(path+".gz"));){
	    		byte[] array = new byte[1024];
			    int number = -1;
			    while((number = in.read(array, 0, array.length)) != -1) {
			    	out.write(array, 0, number);
			    }
			} catch (FileNotFoundException e) {
				LOGGER.error("压缩错误", e);
			} catch (IOException e) {
				LOGGER.error("压缩错误", e);
			}
		}
    }
}
