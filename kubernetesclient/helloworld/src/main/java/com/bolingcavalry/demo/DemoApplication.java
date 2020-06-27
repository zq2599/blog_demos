package com.bolingcavalry.demo;

import com.google.gson.Gson;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RequestMapping(value = "/hello")
    public String hello() throws Exception {

        String kubeConfigPath = "D://temp//config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);

        // the CoreV1Api loads default api-client from global configuration.
        CoreV1Api api = new CoreV1Api();

        V1PodList list =
                api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);

        List<String> rlt = new ArrayList<>();
        rlt.add(new Date().toString());
        rlt.addAll(
                list
                  .getItems()
                  .stream()
                  .map(value -> value.getMetadata().getNamespace()
                                + ":"
                                + value.getMetadata().getName())
                  .collect(Collectors.toList()));
        return new Gson().toJson(rlt);
    }

}
