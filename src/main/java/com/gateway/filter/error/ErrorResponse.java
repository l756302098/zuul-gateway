package com.gateway.filter.error;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

@Component
public class ErrorResponse extends ZuulFilter{

    Logger logger= LoggerFactory.getLogger(this.getClass());

    @Override
    public String filterType() {
        return "error";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        return context.getThrowable() != null;
    }


    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable ex = ctx.getThrowable();
        try {
            String errorCause="Zuul-Error-Unknown-Cause";
            int responseStatusCode;

            if (ex instanceof ZuulException) {
                String cause = ((ZuulException) ex).errorCause;
                if(cause!=null) errorCause = cause;
                responseStatusCode = ((ZuulException) ex).nStatusCode;

                Enumeration<String> headerIt = ctx.getRequest().getHeaderNames();
                StringBuilder sb = new StringBuilder(ctx.getRequest().getRequestURI()+":"+errorCause);
                while (headerIt.hasMoreElements()) {
                    String name = (String) headerIt.nextElement();
                    String value = ctx.getRequest().getHeader(name);
                    sb.append("REQUEST:: > " + name + ":" + value+"\n");
                }
                logger.error(sb.toString());
            }else{
                responseStatusCode = 500;
            }

            ctx.setResponseStatusCode(responseStatusCode);


            ctx.addZuulResponseHeader("Content-Type", "application/json; charset=utf-8");
            ctx.setSendZuulResponse(false);
            ctx.setResponseBody("{\"Message\":\""+errorCause+"\"}");
        } finally {
            return null;
        }
    }


}
