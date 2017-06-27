package com.brmayi.epiphany.pool;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brmayi.epiphany.exception.EpiphanyException;

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
public class LimitedKeyedPoolableObjectFactory extends BaseKeyedPoolableObjectFactory<String, Runnable> {  
	private static final Logger logger = LoggerFactory.getLogger(LimitedKeyedPoolableObjectFactory.class);
	
	private static final int MAX_NUM_ACTIVE = 100;
	
    private static final KeyedPoolableObjectFactory<String, Runnable> limitedFactory = new LimitedKeyedPoolableObjectFactory();   
    
    private static final KeyedObjectPoolFactory<String, Runnable> limitedPoolFactory = new StackKeyedObjectPoolFactory<String, Runnable>(limitedFactory);  

    public static KeyedObjectPool<String, Runnable> objectPool = limitedPoolFactory.createPool();
    
	@Override
	public Runnable makeObject(String paramK) throws EpiphanyException {
		try {
			if(objectPool.getNumActive()>=MAX_NUM_ACTIVE) {
				logger.error("limitedObjectPool active exceeded!");
				objectPool.clear();
				return null;
			}
			@SuppressWarnings("unchecked")
			Class<Runnable> cls = (Class<Runnable>) Class.forName(paramK);  
			Runnable obj = cls.newInstance();  
			logger.info("create class:{}", paramK);
			 return obj;
		} catch(Exception e) {
			logger.error("limitedObjectPool exception", e);
			throw new EpiphanyException(e);
		}
	}
	
	@Override
	public void destroyObject(String key, Runnable obj){
		obj = null;
	}
} 