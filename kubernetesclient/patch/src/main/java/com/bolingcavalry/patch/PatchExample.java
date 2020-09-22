package com.bolingcavalry.patch;

import com.google.gson.GsonBuilder;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1Deployment;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;

@SpringBootApplication
@RestController
@Slf4j
public class PatchExample {

    static String DEPLOYMENT_NAME = "test123";

    static String NAMESPACE = "default";

    static String deployStr, jsonStr, mergeStr, strategicStr, applyYamlStr;
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

        // 部署用的JSON字符串
        deployStr = new ClassPathResourceReader("deploy.json").getContent();

        // json patch用的JSON字符串
        jsonStr = new ClassPathResourceReader("json.json").getContent();

        // merge patch用的JSON字符串，和部署的JSON相比：replicas从1变成2，增加一个名为from的label，值为merge
        mergeStr = new ClassPathResourceReader("merge.json").getContent();

        // strategic merge patch用的JSON字符串
        strategicStr = new ClassPathResourceReader("strategic.json").getContent();

        // server side apply用的JSON字符串
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
                .deserialize(deployStr, ExtensionsV1beta1Deployment.class);


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
     * 通用patch方法，fieldManager和force都默认为空
     * @param patchFormat patch类型，一共有四种
     * @param jsonStr patch的json内容
     * @return patch结果对象转成的字符串
     * @throws Exception
     */
    private String patch(String patchFormat, String jsonStr) throws Exception {
        return patch(patchFormat,  DEPLOYMENT_NAME, NAMESPACE, jsonStr, null, null);
    }

    /**
     * 通用patch方法
     * @param patchFormat patch类型，一共有四种
     * @param deploymentName deployment的名称
     * @param namespace namespace名称
     * @param jsonStr patch的json内容
     * @param fieldManager server side apply用到
     * @param force server side apply要设置为true
     * @return patch结果对象转成的字符串
     * @throws Exception
     */
    private String patch(String patchFormat, String deploymentName, String namespace, String jsonStr, String fieldManager, Boolean force) throws Exception {
        // 创建api对象，指定格式是patchFormat
        ApiClient patchClient = ClientBuilder
                .standard()
                .setOverridePatchFormat(patchFormat)
                .build();

        log.info("start deploy : " + patchFormat);

        // 开启debug便于调试，生产环境慎用！！！
        patchClient.setDebugging(true);

        // 创建deployment
        ExtensionsV1beta1Deployment deployment = new ExtensionsV1beta1Api(patchClient)
                .patchNamespacedDeployment(
                        deploymentName,
                        namespace,
                        new V1Patch(jsonStr),
                        null,
                        null,
                        fieldManager,
                        force
                );

        log.info("end deploy : " + patchFormat);

        return new GsonBuilder().setPrettyPrinting().create().toJson(deployment);
    }

    /**
     * JSON patch格式的关系
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/patch/json", method = RequestMethod.GET)
    public String json() throws Exception {
        return patch(V1Patch.PATCH_FORMAT_JSON_PATCH, jsonStr);
    }

    @RequestMapping(value = "/patch/fullmerge", method = RequestMethod.GET)
    public String fullmerge() throws Exception {
        return patch(V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH, mergeStr);
    }

    @RequestMapping(value = "/patch/partmerge", method = RequestMethod.GET)
    public String partmerge() throws Exception {
        return patch(V1Patch.PATCH_FORMAT_JSON_MERGE_PATCH, strategicStr);
    }

    @RequestMapping(value = "/patch/strategic", method = RequestMethod.GET)
    public String strategic() throws Exception {
        return patch(V1Patch.PATCH_FORMAT_STRATEGIC_MERGE_PATCH, strategicStr);
    }

    @RequestMapping(value = "/patch/apply", method = RequestMethod.GET)
    public String apply() throws Exception {
        return patch(V1Patch.PATCH_FORMAT_APPLY_YAML,  DEPLOYMENT_NAME, NAMESPACE, applyYamlStr, "example-field-manager", true);
    }

    @RequestMapping(value = "/version")
    public String version() throws Exception {
        return "1.16";
    }

    @RequestMapping(value = "/test")
    public String test() throws Exception {
        return new ClassPathResourceReader("json.json").getContent();
    }

    public static void main(String[] args) {
        SpringApplication.run(PatchExample.class, args);
    }
}