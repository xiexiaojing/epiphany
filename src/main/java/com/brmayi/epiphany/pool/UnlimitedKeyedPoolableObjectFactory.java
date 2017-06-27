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
public class UnlimitedKeyedPoolableObjectFactory extends BaseKeyedPoolableObjectFactory<String, Runnable> {  
	private static final Logger logger = LoggerFactory.getLogger(UnlimitedKeyedPoolableObjectFactory.class);
    
    private static final KeyedPoolableObjectFactory<String, Runnable> unlimitedFactory = new UnlimitedKeyedPoolableObjectFactory();   
    
    private static final KeyedObjectPoolFactory<String, Runnable> unlimitedPoolFactory = new StackKeyedObjectPoolFactory<String, Runnable>(unlimitedFactory);  

    public static KeyedObjectPool<String, Runnable> objectPool = unlimitedPoolFactory.createPool();
    
	@Override
	public Runnable makeObject(String paramK) throws EpiphanyException {
		try {
			@SuppressWarnings("unchecked")
			Class<Runnable> cls = (Class<Runnable>) Class.forName(paramK);  
			Runnable obj = cls.newInstance();  
			logger.info("create class:{}", paramK);
	        return obj;
		} catch(Exception e) {
			logger.error("unlimitedObjectPool exception", e);
			throw new EpiphanyException(e);
		}
	}
	
	@Override
	public void destroyObject(String key, Runnable obj) throws Exception {
		obj = null;
	}
} 