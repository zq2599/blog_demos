package com.bolingcavalry.yaml;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.print.attribute.standard.MediaSize;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;

@SpringBootApplication
@RestController
@Slf4j
public class YamlApplication {

    /**
     * 本次实战用到的namespace，和namespace.yaml、service.yaml文件中的一致
     */
    private final static String NAMESPACE = "yaml";

    /**
     * 本次实战用到的service的名称，和service.yaml文件中的一致
     */
    private final static String SERVICE_NAME = "test-service";



    /**
     * 默认的全局设置
     * @return
     * @throws Exception
     */
    @PostConstruct
    private void setDefaultApiClient() throws Exception {
        // 存放K8S的config文件的全路径
        String kubeConfigPath = "/Users/zhaoqin/temp/202007/05/config";
        // 以config作为入参创建的client对象，可以访问到K8S的API Server
        ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                .build();

        // 会打印和API Server之间请求响应的详细内容，生产环境慎用
        client.setDebugging(true);

        // 创建操作类
        Configuration.setDefaultApiClient(client);

        // 规避已知问题：https://github.com/kubernetes-client/java/issues/474
        // 如果应用是war包，或者运行在junit等涉及到反射类型的class-loader场景下，Yaml.load方法有可能不生效，
        // 此时要执行Yaml.addModelMap来规避此问题
        Yaml.addModelMap("v1", "Namespace", V1Namespace.class);
        Yaml.addModelMap("v1", "Service", V1Service.class);
    }

    /**
     * 将OpenAPI实例转成yaml格式的字符串，通过日志打印出来，再控制浏览器下载此文件
     * @param httpServletResponse 本次会话的response对象，用于下载文件的输出
     * @param dumpObject OpenAPI实例
     * @param fileName 浏览器下载的文件名
     * @throws Exception
     */
    private void writeResponse(HttpServletResponse httpServletResponse, Object dumpObject, String fileName) throws Exception {
        String yaml = Yaml.dump(dumpObject);

        log.info(yaml);

        byte[] bytes = yaml.getBytes("UTF-8");

        httpServletResponse.setHeader("content-type", "application/yaml");
        httpServletResponse.setContentType("application/yaml");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        httpServletResponse.setHeader("Content-Length", "" + bytes.length);

        httpServletResponse.getOutputStream().write(bytes);
    }

    @RequestMapping(value = "/yaml/load")
    public String load() throws Exception {

        CoreV1Api api = new CoreV1Api();

        // 通过yaml文件创建namespace实例
        V1Namespace namespace = (V1Namespace) Yaml.load(new ClassPathResource("namespace.yaml").getFile());

        // 创建namespace资源
        api.createNamespace(namespace, null, null, null);

        // 通过yaml文件创建service实例
        V1Service service = (V1Service) Yaml.load(new ClassPathResource("service.yaml").getFile());

        // 创建service资源
        api.createNamespacedService(NAMESPACE, service, null, null, null);

        return "load operation success " + new Date();
    }

    @RequestMapping(value = "/yaml/getnamespace")
    @ResponseBody
    public String getNamespace(HttpServletResponse httpServletResponse) throws Exception {
        // 查找名为yaml的namespace
        V1Namespace namespace = new CoreV1Api().readNamespace(NAMESPACE, null, null, null);
        // 通过Yaml.dump方法将资源对象转成Yaml格式的字符串，在日志中打印，然后在浏览器以yaml文件的格式下载
        writeResponse(httpServletResponse, namespace, "namespace.yaml");

        return "getnamespace operation success " + new Date();
    }

    @RequestMapping(value = "/yaml/getservice")
    @ResponseBody
    public String getService(HttpServletResponse httpServletResponse) throws Exception {
        // 在yaml这个namespace下，查找名为test-service的service
        V1Service service = new CoreV1Api().readNamespacedService(SERVICE_NAME, NAMESPACE, null, null, null);
        // 通过Yaml.dump方法将资源对象转成Yaml格式的字符串，在日志中打印，然后在浏览器以yaml文件的格式下载
        writeResponse(httpServletResponse, service, "service.yaml");

        return "getservice operation success " + new Date();
    }

    @RequestMapping(value = "/yaml/clear")
    public String clear() throws Exception {
        CoreV1Api coreV1Api = new CoreV1Api();

        // 删除service
        coreV1Api.deleteNamespacedService(SERVICE_NAME, NAMESPACE, null, null, null, null, null, null);

        // 删除namespace
        try {
            coreV1Api.deleteNamespace(NAMESPACE, null, null, null, null, null, null);
        } catch (Exception e)
        {
            log.error("delete namespace error", e);
        }

        return "clear finish, " + new Date();
    }

    public static void main(String[] args) {
        SpringApplication.run(YamlApplication.class, args);
    }
}
