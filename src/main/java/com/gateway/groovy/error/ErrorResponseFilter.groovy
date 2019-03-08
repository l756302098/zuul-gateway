package com.gateway.groovy.error

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext
import com.netflix.zuul.exception.ZuulException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

public class ErrorResponseFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(this.getClass())

    @Override
    String filterType() {
        return 'error'
    }

    @Override
    int filterOrder() {
        return 10
    }

    @Override
    boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getThrowable() != null;
    }


    @Override
    Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable ex = ctx.getThrowable()
        try {
            String errorCause="Zuul-Error-Unknown-Cause";
            int responseStatusCode;

            if (ex instanceof ZuulException) {
                String cause = ex.errorCause
                if(cause!=null) errorCause = cause;
                responseStatusCode = ex.nStatusCode;

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
            ctx.setSendZuulResponse(false)
            ctx.setResponseBody("{\"Message\":\""+errorCause+"\"}");
        } finally {
            return null;
        }
    }


}
