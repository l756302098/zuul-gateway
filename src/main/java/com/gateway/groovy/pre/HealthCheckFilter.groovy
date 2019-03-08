package com.gateway.groovy.pre

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants

import javax.servlet.http.HttpServletResponse

public class HealthCheckFilter extends ZuulFilter{

    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    public Object uri() {
        return "health1";
    }

    @Override
    boolean shouldFilter() {
        String path = RequestContext.currentContext.getRequest().getRequestURI()
        logger.info("path:{}",path)
        return path.equalsIgnoreCase(uri())||path.toLowerCase().endsWith(uri());
    }

    public int filterOrder(){
        return 0;
    }

    public String responseBody() {
        return "健康 ok";
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // Set the default response code for static filters to be 200
        HttpServletResponse response = ctx.getResponse()
        ctx.getResponse().setStatus(200);
        ctx.getResponse().setContentType('application/xml')

        // first StaticResponseFilter instance to match wins, others do not set body and/or status
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(responseBody())
            ctx.setSendZuulResponse(false);
        }
        return null;
    }

}
