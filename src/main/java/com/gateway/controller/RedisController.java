package com.gateway.controller;

import com.gateway.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("redis")
public class RedisController {

    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/set/add")
    public Result setAdd(@RequestParam(value = "key") String key,
                           @RequestParam(value = "value") String value) {

        Result result = new Result();

        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(value)){
            result.setCode(420);
            result.setMsg("Missing parameters");
        }
        redisTemplate.opsForSet().add(key,value);
        result.setCode(200);
        result.setMsg("Success");
        return result;
    }

    @RequestMapping("/set/remove")
    public Result setRemove(@RequestParam(value = "key") String key,
                           @RequestParam(value = "value") String value) {

        Result result = new Result();

        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(value)){
            result.setCode(420);
            result.setMsg("Missing parameters");
        }
        redisTemplate.opsForSet().remove(key,value);
        result.setCode(200);
        result.setMsg("Success");
        return result;
    }

    @RequestMapping("/set/all")
    public Result setRemove(@RequestParam(value = "key") String key) {

        Result result = new Result();

        if(StringUtils.isEmpty(key)){
            result.setCode(420);
            result.setMsg("Missing parameters");
        }
        Set<String> resultSet =  redisTemplate.opsForSet().members(key);
        result.setCode(200);
        result.setMsg("Success");
        result.setData(resultSet);
        return result;
    }


}
