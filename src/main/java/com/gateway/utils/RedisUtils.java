package com.gateway.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    private static RedisUtils redisUtils;

    public static Boolean isMember(String key,String value){
        return redisUtils.redisTemplate.opsForSet().isMember(key,value);
    }

    public static boolean hhasKey(String key, String field){
        return redisUtils.redisTemplate.opsForHash().hasKey(key,field);
    }

    public static Object hget(String key, String field){
        return redisUtils.redisTemplate.opsForHash().get(key,field);
    }

    @PostConstruct
    public void init() {
        redisUtils = this;
        redisUtils.redisTemplate = this.redisTemplate;
    }


}
