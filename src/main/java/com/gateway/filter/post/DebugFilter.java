package com.gateway.filter.post;

import com.alibaba.fastjson.JSON;
import com.gateway.model.Result;
import com.gateway.utils.GatewayCodeEnum;
import com.gateway.utils.RequestUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

public class DebugFilter   extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(DebugFilter.class);
    private static boolean isDebug = true;

    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1000;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest request = ctx.getRequest();
            InputStream inputStream = request.getInputStream();
            String reqBbody = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));

            if(isDebug){
                String ipAddr = RequestUtil.getIpAddr(request);
                logger.info("request ip:\t" + ipAddr);
                // 打印请求方法，路径
                logger.info("request url:\t" + request.getMethod() + "\t" + request.getRequestURL().toString());
                Map<String, String[]> map = request.getParameterMap();
                // 打印请求url参数
                if (map != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("request parameters:\t");
                    for (Map.Entry<String, String[]> entry : map.entrySet()){
                        sb.append("[" + entry.getKey() + "=" + printArray(entry.getValue()) + "]");
                    }
                    logger.info(sb.toString());
                }
                // 打印请求json参数
                if (reqBbody != null) {
                    logger.info("request body:\t" + reqBbody);
                }
            }

            // 打印response
            InputStream out = ctx.getResponseDataStream();
            String outBody = StreamUtils.copyToString(out, Charset.forName("UTF-8"));
            if (outBody != null){
                logger.info("response body:\t" + outBody);
            }

            logger.info("host:\t"+ctx.getRouteHost());


            //ctx.setResponseBody(outBody);

            boolean isAdd = false;
            Object isObj = ctx.get("isSuccess");
            //logger.info("isSuccess:{}",isObj);
            if(isObj!=null){
                boolean isSuccess = (boolean)isObj;
                if(!isSuccess){
                    isAdd = true;
                }
            }

            if(isAdd){
                Integer resultCode = (Integer)ctx.get("resultCode");
                Result result = new Result();
                result.setCode(resultCode);
                result.setMsg(GatewayCodeEnum.valueOf(resultCode).getInfo());
                result.setData("");
                ctx.setResponseBody(JSON.toJSONString(result));
            }else{
                ctx.setResponseBody(outBody);
            }

        }catch (Exception e){
            logger.error(e.getStackTrace().toString());
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
