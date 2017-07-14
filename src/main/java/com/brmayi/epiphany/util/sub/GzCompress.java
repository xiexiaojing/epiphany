package com.brmayi.epiphany.util.sub;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.util.GzCompressUtil;
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
 *      @author 静儿(987489055@qq.com)
 *
 */
public class GzCompress implements Runnable {
	private String path;
	private final static Logger LOGGER = LoggerFactory.getLogger(GzCompressUtil.class);
	/**
     * hadoop支持gz格式自解压，gz可以压缩到原文本文件的1/7
     * 
     * @param path 压缩的文件路径
     */
    public void setPath(String path) {
		this.path = path;
	}
        
	/**
     * hadoop支持gz格式自解压，gz可以压缩到原文本文件的1/7
     * 
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