package com.bolingcavalry.security.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class ESService {

//    @Resource(name="clientByPasswd")
    @Resource(name="clientByApiKey")
    private ElasticsearchClient elasticsearchClient;

    public void addIndex(String name) throws IOException {
        elasticsearchClient.indices().create(c -> c.index(name));
    }

    public boolean indexExists(String name) throws IOException {
        return elasticsearchClient.indices().exists(b -> b.index(name)).value();
    }

    public void delIndex(String name) throws IOException {
        elasticsearchClient.indices().delete(c -> c.index(name));
    }
}
