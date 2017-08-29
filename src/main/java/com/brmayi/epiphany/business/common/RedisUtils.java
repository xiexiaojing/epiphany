package com.brmayi.epiphany.business.common;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisUtils {
    
    public static void clearRedis(RedisTemplate<String, ?> redisTemplate, String redisNoKey){
    		String maxKey = new StringBuilder("max").append(redisNoKey).toString();
		String minKey = new StringBuilder("min").append(redisNoKey).toString();
		String numKey = new StringBuilder("n").append(redisNoKey).toString();
		String queueListKey = new StringBuilder("ps").append(redisNoKey).toString();
		redisTemplate.delete(maxKey);
		redisTemplate.delete(minKey);
		redisTemplate.delete(numKey);
		redisTemplate.delete(queueListKey);
    }
}
