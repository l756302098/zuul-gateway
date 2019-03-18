package com.gateway.filter.pre;

import com.gateway.utils.*;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.data.redis.core.RedisTemplate;
import com.alibaba.fastjson.*;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * API验签
 */
//@Component
public class ValidateFilter extends ZuulFilter {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 210;
    }

    @Override
    public boolean shouldFilter() {

        RequestContext ctx= RequestContext.getCurrentContext();
        HttpServletRequest req=ctx.getRequest();
        //获取请求的URL
        String requestURL = req.getRequestURI();
        //获取请求的ip地址
        String ip = RequestUtil.getIpAddr(req);

        logger.info("request api filter >> ip: {}, url: {}, params: {}",ip, requestURL, JSON.toJSON(req.getParameterMap()));

        //验证ip白名单
        if(RedisUtils.isMember("IPWHITESET", ip)){
            logger.info(">> IP {} is in white list...",ip);
            return false;
        }

        //验证URL白名单
        if(RedisUtils.isMember("URLWHITESET", requestURL)){
            logger.info(">> URL {} is in our white list...");
            return false;
        }

        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx= RequestContext.getCurrentContext();
        HttpServletRequest req=ctx.getRequest();

        //获取请求的URL
        String requestURL = req.getRequestURI();

        logger.info("request api filter >>  url: {}, params: {}", requestURL,JSON.toJSON(req.getParameterMap()));

        Map<String , Object> map = new HashMap<>();
        map.put("code", "40010");
        map.put("msg", "签名校验失败");

        String tsp = req.getParameter("timestamp");
        String appKey = req.getParameter("key");
        String sign = req.getParameter("sign");

        //timestamp, key, sign 为验签必传参数
        if(StringUtils.isEmpty(appKey) || StringUtils.isEmpty(sign) || StringUtils.isEmpty(tsp)){
            logger.info("验签为空");
            ctx.setResponseStatusCode(401);
            ctx.setSendZuulResponse(false);
            ctx.set("isSuccess", false);
            ctx.set("resultCode", GatewayCodeEnum.SIGN_NOT_VALID.getValue());
            return null;
        }

        //是否开启防重放
        if("true".equalsIgnoreCase("true")){

            Long timestamp = Long.valueOf(tsp);
            Long interval = (System.currentTimeMillis() - timestamp)/1000;
            Long apiInterval = Long.parseLong(EnvUtils.getProperty("api.interval.time"));

            logger.info(">> 相差时间 {}",interval);

            //当前请求时间大于api限制的请求时间
            if(interval > apiInterval){
                //停止请求
                ctx.setResponseStatusCode(401);
                ctx.setSendZuulResponse(false);
                ctx.set("isSuccess", false);
                ctx.set("resultCode", GatewayCodeEnum.SIGN_NOT_VALID.getValue());
                return null;
            }
        }

        Map<String, String[]> paramsMap = req.getParameterMap();

        Map<String, String> params = new HashMap<>();
        for (String key: paramsMap.keySet()) {
            params.put(key, paramsMap.get(key)[0]);
        }

        String androidAppKey = EnvUtils.getProperty("api.adroid.appKey");
        String iosAppKey = EnvUtils.getProperty("api.ios.appKey");
        String wxAppKey = EnvUtils.getProperty("api.wx.appKey");
        String appSecret = null;

        if(androidAppKey.equals(appKey)){
            appSecret =  EnvUtils.getProperty("api.adroid.appSecret");
        }else if(iosAppKey.equals(appKey)){
            appSecret =  EnvUtils.getProperty("api.ios.appSecret");
        }else if(wxAppKey.equals(appKey)){
            appSecret =  EnvUtils.getProperty("api.wx.appSecret");
            logger.info("requet api platform is weixin.");
        }else {
           //停止请求
            ctx.setResponseStatusCode(401);
            ctx.setSendZuulResponse(false);
            ctx.set("isSuccess", false);
            ctx.set("resultCode", GatewayCodeEnum.SIGN_NOT_VALID.getValue());
            return null;
        }

        if(SignParamsUtils.verify(params, appSecret)){
            logger.info(">> validate sign is success");
        }else{
            //停止请求
            logger.error(">> validate sign is fail url: {}, params: {}",requestURL,JSON.toJSON(req.getParameterMap()));
            ctx.setResponseStatusCode(401);
            ctx.setSendZuulResponse(false);
            ctx.set("isSuccess", false);
            ctx.set("resultCode", GatewayCodeEnum.SIGN_NOT_VALID.getValue());
        }

        return null;
    }

}
