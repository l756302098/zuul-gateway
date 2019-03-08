package com.gateway.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class EnvUtils {

    @Autowired
    private Environment environment;

    private static EnvUtils envUtils;

    @PostConstruct
    public void init() {
        envUtils = this;
        envUtils.environment = this.environment;
    }

    public static String getProperty(String key){
        return envUtils.environment.getProperty(key);
    }

}
