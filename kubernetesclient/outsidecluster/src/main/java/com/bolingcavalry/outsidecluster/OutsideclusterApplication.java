package com.bolingcavalry.outsidecluster;

import com.google.gson.GsonBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;

@SpringBootApplication
@RestController
@Slf4j
public class OutsideclusterApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutsideclusterApplication.class, args);
    }

    @RequestMapping(value = "/hello")
    public V1PodList hello() throws Exception {
        // 存放K8S的config文件的全路径
        String kubeConfigPath = "/Users/zhaoqin/temp/202007/05/config";

        // 以config作为入参创建的client对象，可以访问到K8S的API Server
        ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                .build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // 调用客户端API取得所有pod信息
        V1PodList v1PodList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        // 使用jackson将集合对象序列化成JSON，在日志中打印出来
        log.info("pod info \n{}", new GsonBuilder().setPrettyPrinting().create().toJson(v1PodList));

        return v1PodList;
    }

}
