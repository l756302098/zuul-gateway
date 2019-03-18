package com.gateway.groovy.pre

import com.gateway.utils.RedisUtils
import com.gateway.utils.GatewayCodeEnum
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants
import javax.servlet.http.HttpServletRequest

public class IPBlackFilter  extends ZuulFilter{

    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    boolean shouldFilter() {
        return true;
    }

    public int filterOrder(){
        return 200;
    }

    @Override
    public Object run() {
        RequestContext ctx= RequestContext.getCurrentContext();
        HttpServletRequest req=ctx.getRequest();
        String ipAddr=this.getIpAddr(req);
        logger.info("请求IP地址为：[{}]",ipAddr);
        //配置本地IP白名单，生产环境可放入数据库或者redis中

        if(RedisUtils.isMember("IPBLACKSET",ipAddr)){
            ctx.setResponseStatusCode(401);
            ctx.setSendZuulResponse(false);
            ctx.set("isSuccess", false);
            ctx.set("resultCode", GatewayCodeEnum.IP_FORBIDDEN.getValue());
            logger.error("IP 在黑名单中");
            return null;
        }

        logger.info("IP校验通过。");
        return null;
    }

    /**
     * 获取Ip地址
     * @param request
     * @return
     */
    public  String getIpAddr(HttpServletRequest request){

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
