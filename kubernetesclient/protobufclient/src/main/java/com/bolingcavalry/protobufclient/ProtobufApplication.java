package com.bolingcavalry.protobufclient;

import com.google.gson.GsonBuilder;
import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.ProtoClient.ObjectOrStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.proto.Meta;
import io.kubernetes.client.proto.V1.Namespace;
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

    /**
     * 根据配置文件创建ProtoClient实例
     * @return
     * @throws Exception
     */
    private ProtoClient buildProtoClient() throws Exception {
        // 存放K8S的config文件的全路径
        String kubeConfigPath = "/Users/zhaoqin/temp/202007/05/config";
        // 以config作为入参创建的client对象，可以访问到K8S的API Server
        ApiClient client = ClientBuilder
                .kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                .build();

        // 创建操作类
        return new ProtoClient(client);
    }

    @RequestMapping(value = "/createnamespace/{namespace}", method = RequestMethod.GET)
    public ObjectOrStatus<Namespace> createnamespace(@PathVariable("namespace") String namespace) throws Exception {
        // 创建namespace资源对象
        Namespace namespaceObj =
                Namespace.newBuilder().setMetadata(Meta.ObjectMeta.newBuilder().setName(namespace).build()).build();

        // 通过ProtoClient的create接口在K8S创建namespace
        ObjectOrStatus<Namespace> ns = buildProtoClient().create(namespaceObj, "/api/v1/namespaces", "v1", "Namespace");

        // 使用Gson将集合对象序列化成JSON，在日志中打印出来
        log.info("ns info \n{}", new GsonBuilder().setPrettyPrinting().create().toJson(ns));

        return ns;
    }

    @RequestMapping(value = "/pods/{namespace}", method = RequestMethod.GET)
    public ObjectOrStatus<PodList> pods(@PathVariable("namespace") String namespace) throws Exception {
        // 通过ProtoClient的list接口获取指定namespace下的pod列表
        ObjectOrStatus<PodList> pods = buildProtoClient().list(PodList.newBuilder(), "/api/v1/namespaces/" + namespace + "/pods");

        // 使用Gson将集合对象序列化成JSON，在日志中打印出来
        log.info("pod info \n{}", new GsonBuilder().setPrettyPrinting().create().toJson(pods));

        return pods;
    }
}