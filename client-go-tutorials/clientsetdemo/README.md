### 关于此项目

这是client-go的Clientset客户端的使用demo，功能如下：

1. 新建名为test-clientset的namespace
2. 新建一个deployment，namespace为test-clientset，镜像用tomcat，副本数为2
3. 新建一个service，namespace为test-clientset，类型是NodePort

以上需求使用Clientset客户端实现，完成后咱们用浏览器访问来验证tomcat是否正常；

