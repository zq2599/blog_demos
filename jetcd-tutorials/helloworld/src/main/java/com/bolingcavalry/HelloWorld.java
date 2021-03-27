package com.bolingcavalry;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

import static com.google.common.base.Charsets.UTF_8;

@Slf4j
public class HelloWorld {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String endpoints = "http://192.168.133.218:2379,http://192.168.133.218:2380,http://192.168.133.218:2381";
        String key = "/aaa/foo";
        Client client = Client.builder().endpoints(endpoints.split(",")).build();


        GetResponse response = client.getKVClient()
                .get(ByteSequence.from(key, UTF_8), GetOption.newBuilder().withRevision(2L).build()).get();

        if (response.getKvs().isEmpty()) {
            // key does not exist
            return;
        }

        log.info("key [{}], value [{}]", key, response.getKvs().get(0).getValue().toString(UTF_8));
        log.info("finish");
    }
}
