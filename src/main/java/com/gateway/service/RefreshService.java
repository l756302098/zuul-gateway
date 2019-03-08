package com.gateway.service;

import com.alibaba.nacos.api.naming.listener.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshService {

    public  Logger logger = LoggerFactory.getLogger(RefreshService.class);

    @EventListener(condition = "#event.tag=='freshTime'")
    @Transactional
    public void newProductAdd(Event event) throws Exception {

        logger.info("RefreshService freshTime :{}",event);

    }

}
