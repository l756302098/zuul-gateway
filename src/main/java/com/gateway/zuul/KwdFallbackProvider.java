package com.gateway.zuul;

import com.alibaba.fastjson.JSON;
import com.gateway.model.Result;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 熔断器，避免被后端服务拖垮
 * 需要注意在使用path和url的映射关系来配置路由规则时，对于路由转发的请求不会采用HystrixCommand来包装，所以这类路由请求没有线程隔离和断路器的保护以及负载均衡的能力
 * so 暂时不起作用
 */
@Component
public class KwdFallbackProvider implements FallbackProvider{

    public final static Logger logger = LoggerFactory.getLogger(KwdFallbackProvider.class);

    @Override
    public String getRoute() {
        return "*";
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        logger.warn(String.format("route:%s,exceptionType:%s,stackTrace:%s", route, cause.getClass().getName(), cause.getStackTrace()));
        String message = "";
        if (cause instanceof HystrixTimeoutException) {
            message = "Timeout";
        } else {
            message = "Service exception";
        }
        return fallbackResponse(message);
    }

    public ClientHttpResponse fallbackResponse(String message) {

        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.OK;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return 200;
            }

            @Override
            public String getStatusText() throws IOException {
                return "OK";
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getBody() throws IOException {
                Result result = new Result();
                result.setCode(500);
                result.setMsg("Service unavailable:"+message);
                //String bodyText = String.format("{\"code\": 500,\"message\": \"Service unavailable:%s\"}", message);
                String bodyText = JSON.toJSONString(result);
                return new ByteArrayInputStream(bodyText.getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };

    }

}
