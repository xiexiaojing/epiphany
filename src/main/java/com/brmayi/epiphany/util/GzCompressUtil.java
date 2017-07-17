package com.brmayi.epiphany.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.pool.UnlimitedKeyedPoolableObjectFactory;
import com.brmayi.epiphany.util.sub.GzCompress;

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
	private static final ExecutorService fixedThreadPoolForGz = Executors.newFixedThreadPool(6);
    
	  
    /** 
     * 压缩 
     *  
     * @param data 
     *            待压缩数据 
     * @return byte[] 压缩后的数据 
     */  
    public static byte[] compress(byte[] data) {
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
	 * 执行gz压缩
	 * @param path 文件路径
	 * @throws EpiphanyException 抛出压缩异常
	 */
    @Deprecated
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
    
    /**
     * 同步压缩，为了好计算是否终止状态，将来真正有需求可以支持同步和异步两种模式
     * @param path 文件路径
	 * @throws EpiphanyException 抛出压缩异常
     */
	public static void compressFile(String path)  throws EpiphanyException {
       	try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
    		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(path+".gz"));){
    		byte[] array = new byte[1024];
		    int number = -1;
		    while((number = in.read(array, 0, array.length)) != -1) {
		    	out.write(array, 0, number);
		    }
		} catch (FileNotFoundException e) {
			LOGGER.error("压缩错误", e);
			throw new EpiphanyException(e);
		} catch (IOException e) {
			LOGGER.error("压缩错误", e);
			throw new EpiphanyException(e);
		}
	}
}
