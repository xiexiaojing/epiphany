package com.brmayi.epiphany.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.brmayi.epiphany.exception.EpiphanyException;
import com.brmayi.epiphany.util.GzCompressUtil;
/**
 * 	序列化工具
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
public class ObjectRedisSerializer implements RedisSerializer<Object> {
	/**
	 * 对象序列化为字符串
	 * @param obj jvm对象
	 * @return 序列化后的字符串
	 * @throws EpiphanyException 非检查异常
	 */
	@Override
	public byte[] serialize(Object obj) throws SerializationException {
		if(obj==null) {
			return null;
		}
        try ( ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);  ){
            objectOutputStream.writeObject(obj); 
            return GzCompressUtil.compress(byteArrayOutputStream.toByteArray()); 
        } catch (Exception e) {
           throw new EpiphanyException(e);
        }
	}

    
    /**
     * 字符串反序列化为对象
     * @param redisData 序列化的字符串
     * @return jvm对象
     * @throws EpiphanyException 非检查异常
     */
	@Override
	public Object deserialize(byte[] redisData) throws SerializationException {
		if(redisData==null) {
			return null;
		}
        Object newObj = null;
        redisData = GzCompressUtil.decompress(redisData);
        try ( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redisData);  
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream); ){
            newObj = objectInputStream.readObject();
        } catch (Exception e) {
        	throw new EpiphanyException(e);
        }
        return newObj;
	}

}
