package com.bolingcavalry.fluent;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.util.Date;

@SpringBootApplication
@RestController
@Slf4j
public class FluentStyleApplication {

    private final static String NAMESPACE = "fluent";

    public static void main(String[] args) {
        SpringApplication.run(FluentStyleApplication.class, args);
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

        // 会打印和API Server之间请求响应的详细内容，生产环境慎用
        client.setDebugging(true);

        // 创建操作类
        Configuration.setDefaultApiClient(client);
    }

    @RequestMapping(value = "/fluent/createnamespace")
    public V1Namespace createnamespace() throws Exception {

        V1Namespace v1Namespace = new V1NamespaceBuilder()
                .withNewMetadata()
                .withName(NAMESPACE)
                .addToLabels("label1", "aaa")
                .addToLabels("label2", "bbb")
                .endMetadata()
                .build();

        return new CoreV1Api().createNamespace(v1Namespace, null, null, null);
    }

    @RequestMapping(value = "/fluent/createdeployment")
    public ExtensionsV1beta1Deployment createdeployment() throws Exception {
        ExtensionsV1beta1Deployment v1Deployment = new ExtensionsV1beta1DeploymentBuilder()
                // meta设置
                .withNewMetadata()
                    .withName("nginx")
                .endMetadata()

                // spec设置
                .withNewSpec()
                    .withReplicas(1)
                    // spec的templat
                    .withNewTemplate()
                        // template的meta
                        .withNewMetadata()
                            .addToLabels("name", "nginx")
                        .endMetadata()

                        // template的spec
                        .withNewSpec()
                            .addNewContainer()
                                .withName("nginx")
                                .withImage("nginx:1.18.0")
                                .addToPorts(new V1ContainerPort().containerPort(80))
                            .endContainer()
                        .endSpec()

                    .endTemplate()
                .endSpec()
                .build();

        return new ExtensionsV1beta1Api().createNamespacedDeployment(NAMESPACE, v1Deployment, null, null, null);
    }

    @RequestMapping(value = "/fluent/createservice")
    public V1Service createservice() throws Exception {
        V1Service v1Service = new V1ServiceBuilder()
                // meta设置
                .withNewMetadata()
                    .withName("nginx")
                .endMetadata()

                // spec设置
                .withNewSpec()
                    .withType("NodePort")
                    .addToPorts(new V1ServicePort().port(80).nodePort(30103))
                    .addToSelector("name", "nginx")
                .endSpec()
                .build();

        return new CoreV1Api().createNamespacedService(NAMESPACE, v1Service, null, null, null);
    }

    @RequestMapping(value = "/fluent/clear")
    public String clear() throws Exception {

        // 删除deployment
        try {
            new ExtensionsV1beta1Api().deleteNamespacedDeployment("nginx", NAMESPACE, null, null, null, null, null, null);
        } catch (Exception e)
        {
            log.error("delete deployment error", e);
        }

        CoreV1Api coreV1Api = new CoreV1Api();

        // 删除service
        coreV1Api.deleteNamespacedService("nginx", NAMESPACE, null, null, null, null, null, null);

        // 删除namespace
        try {
            coreV1Api.deleteNamespace(NAMESPACE, null, null, null, null, null, null);
        } catch (Exception e)
        {
            log.error("delete namespace error", e);
        }

        return "clear finish, " + new Date();
    }
}
