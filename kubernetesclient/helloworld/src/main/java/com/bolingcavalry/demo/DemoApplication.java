package com.bolingcavalry.demo;

import com.google.gson.Gson;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RequestMapping(value = "/hello")
    public List<String> hello() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // 调用客户端API取得所有pod信息
        V1PodList v1PodList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        // 使用Gson将集合对象序列化成JSON，在日志中打印出来
        log.info("pod info \n{}", new Gson().toJson(v1PodList));

        return v1PodList
                .getItems()
                .stream()
                .map(value ->
                        value.getMetadata().getNamespace()
                        + ":"
                        + value.getMetadata().getName())
                .collect(Collectors.toList());
    }

}
