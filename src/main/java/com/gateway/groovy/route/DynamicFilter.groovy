package com.gateway.groovy.route

import com.netflix.zuul.ZuulFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

public   class DynamicFilter  extends ZuulFilter{

    private static final Logger logger = LoggerFactory.getLogger(this.getClass())

    private static boolean isDebug = false;

    public boolean shouldFilter() {
        return isDebug;
    }


    public Object run() {
        //System.out.println("=========  这一个是动态加载的过滤器：DynamicFilter")
        logger.info("info =========  这一个是动态加载的过滤器：DynamicFilter")
        return null;
    }

    public String filterType() {
        return FilterConstants.ROUTE_TYPE;
    }

    public int filterOrder() {
        return -20;
    }

}
