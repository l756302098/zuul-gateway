package com.gateway.groovy.pre

import com.alibaba.fastjson.JSONObject
import com.gateway.model.Result
import com.gateway.utils.JwtUtils
import com.gateway.utils.RedisUtils
import com.gateway.utils.GatewayCodeEnum
import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.http.ServletInputStreamWrapper
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.util.StreamUtils

import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import java.nio.charset.Charset

/**
 * 验证token,然后把token中的userId放入参数
 * 1.此过滤器必须放在验签之后，不然会因为添加的参数而验证失败
 * 2.GET和其他的处理方式不同
 * 3.对于登录/发送短信等部分接口不能验证
 */
public class AuthFilter  extends ZuulFilter{

    Logger logger= LoggerFactory.getLogger(this.getClass());

    private static final String tokenKey = "MDZJ_TOKEN";
    private static final String prefix = "mdzj_token_";
    @Autowired
    RedisTemplate redisTemplate;
    private static boolean isDebug = true;
    private static boolean isModifyParam = true;
    //对登录接口/发送短信接口不需要验证
    public static List<String> notAuthurl = Arrays.asList("/uc/login", "/uc/register", "/uc/code_login", "/uc/check_code", "/uc/vitifyCode", "/uc/token/refresh");

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 220;
    }

    @Override
    public boolean shouldFilter() {
        if(isDebug){
            RequestContext ctx= RequestContext.getCurrentContext();
            HttpServletRequest req=ctx.getRequest();
            //获取请求的URL
            String requestURL = req.getRequestURI();
            //验证URL白名单
            if(RedisUtils.isMember("URLWHITESET", requestURL)){
                logger.info(">> URL {} is in our white list...",requestURL);
                return false;
            }
            //对登录接口/发送短信接口不需要验证
            if(notAuthurl.contains(requestURL)){
                logger.info(">> URL {} is in our notAuthurl list...",requestURL);
                return false;
            }

            return true;
        }else{
            return false;
        }

    }

    @Override
    public Object run() {

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        //token对象
        String authorization = request.getHeader("Authorization");
        String token = StringUtils.substringAfter(authorization,"Bearer ");

        logger.info(">> token: {}",token);
        //登录校验逻辑  如果token为null，则直接返回客户端，而不进行下一步接口调用
        if (StringUtils.isBlank(token)) {
            // 过滤该请求，不对其进行路由
            requestContext.setSendZuulResponse(false);
            //返回错误代码
            //requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.setResponseStatusCode(500);
            requestContext.set("isSuccess", false);
            requestContext.set("resultCode", GatewayCodeEnum.TOKEN_NULL.getValue());
            return null;
        }
        //JWT 验证有效性
        Result result = JwtUtils.getUID(token);
        if(result.getCode()== GatewayCodeEnum.TOKEN_INVALID.getValue()){
            logger.error("token 验证失败");
            // 过滤该请求，不对其进行路由
            requestContext.setSendZuulResponse(false);
            //返回错误代码
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.set("isSuccess", false);
            requestContext.set("resultCode", GatewayCodeEnum.TOKEN_INVALID.getValue());
            return null;
        }else if(result.getCode()== GatewayCodeEnum.TOKEN_EXPIRE.getValue()){
            logger.error("token 过期");
            // 过滤该请求，不对其进行路由
            requestContext.setSendZuulResponse(false);
            //返回错误代码
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.set("isSuccess", false);
            requestContext.set("resultCode", GatewayCodeEnum.TOKEN_EXPIRE.getValue());
            return null;
        }
        //从redis 验证
        String ukey = prefix+result.getData().toString();
        if(!RedisUtils.hhasKey(tokenKey,ukey) || !RedisUtils.hget(tokenKey,ukey).toString().equals(token)){
            logger.error("token 从redis 验证失败");
            // 过滤该请求，不对其进行路由
            requestContext.setSendZuulResponse(false);
            //返回错误代码
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            requestContext.set("isSuccess", false);
            requestContext.set("resultCode", GatewayCodeEnum.TOKEN_INVALID.getValue());
            return null;
        }
        logger.info("token 验证成功 userId:{}",result.getData());

        if(!isModifyParam) {
            return null;
        }
        //修改请求添加参数
        String method = request.getMethod();
        if(method.contains("GET")){
            Map<String, List<String>> paramsMap = requestContext.getRequestQueryParams();
            if (paramsMap==null) {
                paramsMap=new HashMap<>();
            }
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(result.getData().toString());
            paramsMap.put("userId",arrayList);
            requestContext.setRequestQueryParams(paramsMap);
        }else{

            try {
                // 获取请求的输入流
                InputStream inputStream = request.getInputStream();
                String reqBbody = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));
                String contentType = request.getContentType();
                logger.info("contentType:{}",contentType);
                if(contentType.contains("form-data")){
                    logger.info("contentType is form-data");

                }else if(contentType.contains("x-www-form-urlencoded")){
                    logger.info("contentType is x-www-form-urlencoded");
                    logger.info("old body" + reqBbody);
                    // 转化成json
                    String newStr = "&userId="+result.getData().toString();
                    reqBbody+=newStr;
                    // 把解密之后的参数放到json里
                    logger.info("new body" + reqBbody);
                    final byte[] reqBodyBytes = reqBbody.getBytes();
                    // 重写上下文的HttpServletRequestWrapper
                    requestContext.setRequest(new HttpServletRequestWrapper(request) {
                        @Override
                        public ServletInputStream getInputStream() throws IOException {
                            return new ServletInputStreamWrapper(reqBodyBytes);
                        }

                        @Override
                        public int getContentLength() {
                            return reqBodyBytes.length;
                        }

                        @Override
                        public long getContentLengthLong() {
                            return reqBodyBytes.length;
                        }
                    });
                }else{
                    logger.info("contentType is json");
                    // 如果body为空初始化为空json
                    if (StringUtils.isBlank(reqBbody)) {
                        reqBbody = "{}";
                    }
                    logger.info("old body" + reqBbody);
                    // 转化成json
                    JSONObject json = JSONObject.parseObject(reqBbody);
                    // 把解密之后的参数放到json里
                    json.put("userId", result.getData().toString());
                    String newBody = json.toString();
                    logger.info("new body" + newBody);
                    final byte[] reqBodyBytes = newBody.getBytes();
                    // 重写上下文的HttpServletRequestWrapper
                    requestContext.setRequest(new HttpServletRequestWrapper(request) {
                        @Override
                        public ServletInputStream getInputStream() throws IOException {
                            return new ServletInputStreamWrapper(reqBodyBytes);
                        }

                        @Override
                        public int getContentLength() {
                            return reqBodyBytes.length;
                        }

                        @Override
                        public long getContentLengthLong() {
                            return reqBodyBytes.length;
                        }
                    });
                }



            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return null;
    }

}
