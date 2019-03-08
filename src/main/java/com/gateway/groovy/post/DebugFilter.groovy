package com.gateway.groovy.post

import com.gateway.utils.RequestUtil
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants
import org.springframework.util.StreamUtils

import javax.servlet.http.HttpServletRequest
import java.nio.charset.Charset

class DebugFilter  extends ZuulFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugFilter.class);

    private static boolean isDebug = true;

    @Override
    String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    int filterOrder() {
        return 1000
    }

    @Override
    boolean shouldFilter() {
        return isDebug;
    }

    @Override
    Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            InputStream inputStream = request.getInputStream();
            String reqBbody = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));

            String ipAddr = RequestUtil.getIpAddr(request);
            LOGGER.info("request ip:\t" + ipAddr);
            // 打印请求方法，路径
            LOGGER.info("request url:\t" + request.getMethod() + "\t" + request.getRequestURL().toString());
            Map<String, String[]> map = request.getParameterMap();
            // 打印请求url参数
            if (map != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("request parameters:\t");
                for (Map.Entry<String, String[]> entry : map.entrySet()){
                    sb.append("[" + entry.getKey() + "=" + printArray(entry.getValue()) + "]");
                }
                LOGGER.info(sb.toString());
            }
            // 打印请求json参数
            if (reqBbody != null) {
                LOGGER.info("request body:\t" + reqBbody);
            }

            // 打印response
            InputStream out = ctx.getResponseDataStream();
            String outBody = StreamUtils.copyToString(out, Charset.forName("UTF-8"));
            if (outBody != null){
                LOGGER.info("response body:\t" + outBody);
            }

            LOGGER.info("host:\t"+ctx.getRouteHost());

            ctx.setResponseBody(outBody);

        }catch (Exception e){
            LOGGER.error(e.getStackTrace());
        }
        return null;
    }

    public String printArray(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
           sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }


}
