package com.gateway.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate redisTemplate;

    private static RedisUtils redisUtils;

    public static Set<String> getSet(String key){
        Set<String> resultSet = redisUtils.redisTemplate.opsForSet().members(key);
        return  resultSet;
    }

    public static Boolean isMember(String key,String value){
        return redisUtils.redisTemplate.opsForSet().isMember(key,value);
    }

    @PostConstruct
    public void init() {
        redisUtils = this;
        redisUtils.redisTemplate = this.redisTemplate;
    }


}
