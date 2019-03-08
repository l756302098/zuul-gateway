# zuul-gateway
基于netflix-zuul开发的网关

## 目的  
利用Spring Cloud Zuul网关逐渐完善微服务框架，达到以下目标  
1>动态路由  
2>限流  
3>认证（暂时不做）  
4>金丝雀测试（暂时不做）  
  
## 功能  
1>IP限流(spring-cloud-zuul-ratelimit)  
2>IP黑名单(Groovy/Filter)  
3>跨域（Groovy/bean）  
4>API鉴权（Groovy/Filter）  
  
## 注意  
一般微服务项目中利用注册中心（Consul/Eureku/Zookeeper）或者ribbon来实现负载均衡，本项  
目利用阿里云的slb或者nginx来实现，后期通过替换后端服务利用注册中心解决。   

## 集成 aliyun  
为了维护方便，将spring cloud 项目与阿里云的EDAS集成，与SLB打通，实现自动化  
主要集成alicloud-ans 和 alibaba-nacos-config  
前者主要替换注册中心，简单易用，后者主要实现动态刷新路由和配置文件
