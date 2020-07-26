package com.bolingcavalry.patch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.PostConstruct;
import java.io.*;

@SpringBootApplication
@RestController
@Slf4j
public class PatchExample {

    static String DEPLOYMENT_NAME = "test123";

    /**
     * 本次实战使用nginx作为测试镜像
     */
    static String IMAGE_NAME = "nginx";

    /**
     * 现部署1.18.0版本
     */
    static String OLD_TAG = "1.18.0";

    /**
     * 更新的时候使用1.19.1版本
     */
    static String NEW_TAG = "1.19.1";

    /**
     * 容器端口
     */
    static int CONTAINER_PORT = 80;

    /**
     * 执行部署的json字符串
     */
    /*
    static String jsonDeploymentStr =
            "{\"kind\":\"Deployment\",\"apiVersion\":\"extensions/v1beta1\",\"metadata\":{\"name\":\"" + DEPLOYMENT_NAME + "\",\"finalizers\":[\"example.com/test\"],\"labels\":{\"run\":\"" + DEPLOYMENT_NAME + "\"}},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"run\":\"" + DEPLOYMENT_NAME + "\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"run\":\"" + DEPLOYMENT_NAME + "\"}},\"spec\":{\"terminationGracePeriodSeconds\":30,\"containers\":[{\"name\":\"" + DEPLOYMENT_NAME + "\",\"image\":\"" + IMAGE_NAME + ":" + OLD_TAG + "\",\"ports\":[{\"containerPort\":" + CONTAINER_PORT + "}],\"resources\":{}}]}},\"strategy\":{}},\"status\":{}}";
    */

    /**
     * 执行json patch的json字符串
     */
    /*
    static String jsonPatchStr =
            "[{\"op\":\"replace\",\"path\":\"/spec/template/spec/terminationGracePeriodSeconds\",\"value\":27}]";
    */

    /**
     * 执行strategic merge path的json字符串
     */
    /*
    static String strategicMergePatchStr =
            "{\"metadata\":{\"$deleteFromPrimitiveList/finalizers\":[\"example.com/test\"]}}";
    */

    /**
     * 执行service side apply的json字符串
     */
    /*
    static String applyYamlStr =
            "{\"kind\":\"Deployment\",\"apiVersion\":\"extensions/v1beta1\",\"metadata\":{\"name\":\"" + DEPLOYMENT_NAME + "\",\"finalizers\":[\"example.com/test\"],\"labels\":{\"run\":\"" + DEPLOYMENT_NAME + "\"}},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"run\":\"" + DEPLOYMENT_NAME + "\"}},\"template\":{\"metadata\":{\"creationTimestamp\":null,\"labels\":{\"run\":\"" + DEPLOYMENT_NAME + "\"}},\"spec\":{\"terminationGracePeriodSeconds\":30,\"containers\":[{\"name\":\"" + DEPLOYMENT_NAME + "\",\"image\":\"" + IMAGE_NAME + ":" + NEW_TAG + "\",\"ports\":[{\"containerPort\":" + CONTAINER_PORT + ", \"protocol\": \"TCP\"}],\"resources\":{}}]}},\"strategy\":{}},\"status\":{}}";
    */

    static String jsonDeploymentStr, jsonPatchStr, strategicMergePatchStr, applyYamlStr;
    /**
     * 默认的全局设置
     * @return
     */
    @PostConstruct
    private void init() throws IOException {
        // 设置api配置
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        // 设置超时时间
        Configuration.getDefaultApiClient().setConnectTimeout(30000);

        // patch实战中用到的字符串
        jsonDeploymentStr = new ClassPathResourceReader("jsonDeployment.json").getContent();
        jsonPatchStr = new ClassPathResourceReader("jsonPatch.json").getContent();
        strategicMergePatchStr = new ClassPathResourceReader("strategicMergePatch.json").getContent();
        applyYamlStr = new ClassPathResourceReader("applyYaml.json").getContent();
    }

    /**
     * 部署一个deployment分为三步(准备api对象，准备body，把body传给api对象创建deployment)
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/patch/deploy", method = RequestMethod.GET)
    public String deploy() throws Exception {
        // 创建API对象
        ExtensionsV1beta1Api api = new ExtensionsV1beta1Api(ClientBuilder.standard().build());

        // 创建body对象
        ExtensionsV1beta1Deployment body = Configuration
                .getDefaultApiClient()
                .getJSON()
                .deserialize(jsonDeploymentStr, ExtensionsV1beta1Deployment.class);


        log.info("start deploy");

        // 在指定的namespace下创建deployment
        ExtensionsV1beta1Deployment deployment =
                api.createNamespacedDeployment(
                        "default",
                        body,
                        null,
                        null,
                        null);

        log.info("end deploy");

        return new GsonBuilder().setPrettyPrinting().create().toJson(deployment);
    }

    /**
     * JSON patch格式的关系
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/patch/jsonpatch", method = RequestMethod.GET)
    public String jsonpatch() throws Exception {
        // 创建api对象，指定格式是json-patch
        ApiClient jsonpatchClient = ClientBuilder
                .standard()
                .setOverridePatchFormat(V1Patch.PATCH_FORMAT_JSON_PATCH)
                .build();

        // 创建deployment
        ExtensionsV1beta1Deployment deployment = new ExtensionsV1beta1Api(jsonpatchClient)
                .patchNamespacedDeployment(
                        DEPLOYMENT_NAME,
                        "default",
                        new V1Patch(jsonPatchStr),
                        null,
                        null,
                        null,
                        null
                );

        return new GsonBuilder().setPrettyPrinting().create().toJson(deployment);
    }

    @RequestMapping(value = "/patch/strategic-merge-patch", method = RequestMethod.GET)
    public String merge() throws Exception {
        // 创建api对象，指定格式是strategic-merge-patch+json
        ApiClient strategicMergePatchClient = ClientBuilder
                .standard()
                .setOverridePatchFormat(V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH)
                .build();

        strategicMergePatchClient.setDebugging(true);

        // 创建deployment
        ExtensionsV1beta1Deployment deployment = new ExtensionsV1beta1Api(strategicMergePatchClient)
                .patchNamespacedDeployment(
                        DEPLOYMENT_NAME,
                        "default",
                        new V1Patch(strategicMergePatchStr),
                        null,
                        null,
                        null,
                        null);

        return new GsonBuilder().setPrettyPrinting().create().toJson(deployment);
    }

    @RequestMapping(value = "/patch/apply", method = RequestMethod.GET)
    public String apply() throws Exception {
        // 创建api对象，指定格式是apply-patch+yaml
        ApiClient applyYamlClient = ClientBuilder
                .standard()
                .setOverridePatchFormat(V1Patch.PATCH_FORMAT_APPLY_YAML)
                .build();

        applyYamlClient.setDebugging(true);

        String rlt = null;

        // 创建deployment
        try {
            ExtensionsV1beta1Deployment deployment = new ExtensionsV1beta1Api(applyYamlClient)
                    .patchNamespacedDeployment(
                            DEPLOYMENT_NAME,
                            "default",
                            new V1Patch(applyYamlStr),
                            null,
                            null,
                            "example-field-manager",
                            true);
            rlt = new GsonBuilder().setPrettyPrinting().create().toJson(deployment);
        }catch (Exception e) {
            e.printStackTrace();
            log.error("error---", e);
            rlt = e.toString();
        }

        return rlt;
    }

    @RequestMapping(value = "/version")
    public String version() throws Exception {
        return "1.7";


    }

    @RequestMapping(value = "/test")
    public String test() throws Exception {
        return new ClassPathResourceReader("jsonPatch.json").getContent();
    }






    public static void main(String[] args) {
        SpringApplication.run(PatchExample.class, args);
    }
}