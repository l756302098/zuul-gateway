package com.gateway.filter.post;

import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

//@Component
public class CorsFilter  extends ZuulFilter {

    Logger logger= LoggerFactory.getLogger(this.getClass());


    @Override
    public String filterType() {
        return "post";
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
        RequestContext context = RequestContext.getCurrentContext();
        List<Pair<String, String>> headers = context.getZuulResponseHeaders();
        //Support CORS
        headers.add(new Pair("Access-Control-Allow-Origin", "*"));
        headers.add(new Pair("Access-Control-Allow-Headers","Content-Type, Accept"));


        return null;
    }

}
