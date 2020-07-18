package com.bolingcavalry.protobufclient;

import com.google.gson.GsonBuilder;
import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.ProtoClient.ObjectOrStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.proto.V1;
import io.kubernetes.client.proto.V1.PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;

@SpringBootApplication
@RestController
@Slf4j
public class ProtobufApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProtobufApplication.class, args);
    }

    @RequestMapping(value = "/pods/{namespace}", method = RequestMethod.GET)
    public ObjectOrStatus<PodList> pods(@PathVariable("namespace") String namespace) throws Exception {
        // 存放K8S的config文件的全路径
        String kubeConfigPath = "D:\\temp\\config";

        // 以config作为入参创建的client对象，可以访问到K8S的API Server
        ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                .build();

        // 创建操作类
        ProtoClient pc = new ProtoClient(client);

        ObjectOrStatus<PodList> pods = pc.list(PodList.newBuilder(), "/api/v1/namespaces/" + namespace + "/pods");

        // 使用Gson将集合对象序列化成JSON，在日志中打印出来
        log.info("pod info \n{}", new GsonBuilder().setPrettyPrinting().create().toJson(pods));

        return pods;
    }

}
