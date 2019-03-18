package com.gateway.controller;

import com.gateway.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
@RefreshScope
public class ConfigController {

    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Value("${freshTime:5000}")
    private String freshTime;

    @Autowired
    private Environment environment;

    @Value("${spring.cloud.nacos.config.endpoint:5000}")
    private String endpoint;

    public void setFreshTime(String freshTime) {
        logger.info("ConfigController setFreshTime:{}",freshTime);
        this.freshTime = freshTime;
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    @ResponseBody
    public String get() {
        logger.info("ConfigController get:{}",freshTime);
        return freshTime;
    }

    @RequestMapping(value = "/get/endpoint", method = RequestMethod.GET)
    @ResponseBody
    public String getEndpoint() {
        logger.info("ConfigController get endpoint:{}",endpoint);
        return endpoint;
    }

    @RequestMapping(value = "/jwt", method = RequestMethod.GET)
    @ResponseBody
    public String maxCon() {
        try {
            String jwt = JwtUtils.createToken(123);
            logger.info("jwt:{}",jwt);
        }catch (Exception e){

        }
        return "jwt";
    }

}
