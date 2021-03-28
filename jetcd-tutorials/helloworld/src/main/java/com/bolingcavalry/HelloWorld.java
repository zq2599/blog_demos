package com.bolingcavalry;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Charsets.UTF_8;

@Slf4j
public class HelloWorld {
    private KV getKVClient(){
        String endpoints = "http://192.168.50.239:2379,http://192.168.50.239:2380,http://192.168.50.239:2381";
        String key = "/aaa/foo";
        Client client = Client.builder().endpoints(endpoints.split(",")).build();
        return client.getKVClient();
    }

    private static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, UTF_8);
    }

    public String get(String key) throws ExecutionException, InterruptedException{
        log.info("start get, key [{}]", key);
        GetResponse response = getKVClient().get(bytesOf(key)).get();

        if (response.getKvs().isEmpty()) {
            log.error("empty value of key [{}]", key);
            return null;
        }

        String value = response.getKvs().get(0).getValue().toString(UTF_8);
        log.info("finish get, key [{}], value [{}]", key, value);
        return value;
    }

    public PutResponse put(String key, String value) throws ExecutionException, InterruptedException {
        log.info("start put, key [{}], value [{}]", key, value);
        return getKVClient().put(bytesOf(key), bytesOf(value)).get();
    }
}
