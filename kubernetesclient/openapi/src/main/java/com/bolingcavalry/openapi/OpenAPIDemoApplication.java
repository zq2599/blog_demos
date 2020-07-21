package com.bolingcavalry.openapi;

import com.google.gson.GsonBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceBuilder;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.FileReader;

@SpringBootApplication
@RestController
@Slf4j
public class OpenAPIDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenAPIDemoApplication.class, args);
    }

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

        // 创建操作类
        Configuration.setDefaultApiClient(client);
    }

    @RequestMapping(value = "/openapi/createnamespace/{namespace}", method = RequestMethod.GET)
    public V1Namespace createnamespace(@PathVariable("namespace") String namespace) throws Exception {

        CoreV1Api coreV1Api = new CoreV1Api();

        V1Namespace v1Namespace = new V1NamespaceBuilder()
                .withNewMetadata()
                .withName(namespace)
                .endMetadata()
                .build();

        V1Namespace ns = coreV1Api.createNamespace(v1Namespace, null, null, null);

        // 使用Gson将集合对象序列化成JSON，在日志中打印出来
        log.info("ns info \n{}", new GsonBuilder().setPrettyPrinting().create().toJson(ns));

        return ns;
    }


    @RequestMapping(value = "/openapi/pods/{namespace}", method = RequestMethod.GET)
    public V1PodList pods(@PathVariable("namespace") String namespace) throws ApiException {

        CoreV1Api apiInstance = new CoreV1Api();

        // String | If 'true', then the output is pretty printed.
        String pretty = null;

        // 订阅事件相关的参数，这里用不上
        Boolean allowWatchBookmarks = false;

        // 连续查找的标志，类似于翻页
        String _continue = null;

        //  字段选择器
        String fieldSelector = "status.phase=Running";

        // 根据标签过滤
        // String labelSelector = "component=kube-apiserver";
        String labelSelector = null;

        Integer limit = null;
        String resourceVersion = null;
        Integer timeoutSeconds = null;
        Boolean watch = false;

        V1PodList v1PodList = apiInstance.listNamespacedPod(namespace,
                pretty,
                allowWatchBookmarks,
                _continue,
                fieldSelector,
                labelSelector,
                limit,
                resourceVersion,
                timeoutSeconds,
                watch);

        // 使用Gson将集合对象序列化成JSON，在日志中打印出来
        log.info("pod info \n{}", new GsonBuilder().setPrettyPrinting().create().toJson(v1PodList));

        return v1PodList;
    }

}