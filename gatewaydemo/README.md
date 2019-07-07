### 关于gatewaydemo工程
gatewaydemo是个普通的SpringCloud技术的demo，下面有三个子项目：
1. eureka：注册中心；
2. provider：服务提供者，提供/hello/time接口，返回当前服务器时间和请求header中的customize_tag字段的值；
3. gateway：网关服务；
