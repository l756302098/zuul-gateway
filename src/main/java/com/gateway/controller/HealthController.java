package com.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    Logger logger= LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/health")
    public String healthCheck() {
        logger.info("health");
        return "health";
    }

}
