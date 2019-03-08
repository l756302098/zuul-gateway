package com.gateway.filter.pre;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * 检测产品列表的uid,来限流
 */
//@Component
public class ProductSellListFilter extends ZuulFilter {

    private Map<String, RateLimiter> map = Maps.newConcurrentMap();

    private static String productSellListURL = "/product/productSell/list";
    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return -190;
    }

    @Override
    public boolean shouldFilter() {
        //匹配路径
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String url = request.getRequestURL().toString();
        logger.info("进入访问过滤器，访问的url:{}，访问的方法：{}",url,request.getMethod());
        if(url.contains(productSellListURL)){
            logger.info("进入产品过滤器");
            return  true;
        }
        return false;
    }

    @Override
    public Object run() {

        try {
            RequestContext context = RequestContext.getCurrentContext();
            HttpServletResponse response = context.getResponse();

            //取出当前请求
            HttpServletRequest request = context.getRequest();
            //从参数中取出key为userId
            String key = request.getParameter("userId");
            if(StringUtils.isEmpty(key)){
                context.setSendZuulResponse(false);
                context.setResponseStatusCode(401);
                return null;
            }


            key = "productList_"+key;
            map.putIfAbsent(key, RateLimiter.create(10.0));


            RateLimiter rateLimiter = map.get(key);
            if (!rateLimiter.tryAcquire()) {
                HttpStatus httpStatus = HttpStatus.TOO_MANY_REQUESTS;

                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.setStatus(httpStatus.value());
                response.getWriter().append(httpStatus.getReasonPhrase());

                context.setSendZuulResponse(false);

                throw new ZuulException(
                        httpStatus.getReasonPhrase(),
                        httpStatus.value(),
                        httpStatus.getReasonPhrase()
                );
            }
        } catch (Exception e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
        return null;

    }

}
