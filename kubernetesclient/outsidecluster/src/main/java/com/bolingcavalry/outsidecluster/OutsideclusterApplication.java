package com.bolingcavalry.outsidecluster;

import com.google.gson.Gson;
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
        String kubeConfigPath = "D:\\temp\\config";

        ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                .build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // 调用客户端API取得所有pod信息
        V1PodList v1PodList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        // 使用jackson将集合对象序列化成JSON，在日志中打印出来
        log.info("pod info \n{}", new Gson().toJson(v1PodList));

        return v1PodList;
    }

}
