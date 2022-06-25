package com.bolingcavalry.fromjson.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.bolingcavalry.fromjson.service.EsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

@Service
public class EsServiceImpl implements EsService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public void create(String name,
                       Function<IndexSettings.Builder, ObjectBuilder<IndexSettings>> settingFn,
                       Function<TypeMapping.Builder, ObjectBuilder<TypeMapping>> mappingFn) throws IOException {
        elasticsearchClient
                .indices()
                .create(c -> c
                        .index(name)
                        .settings(settingFn)
                        .mappings(mappingFn)
                );
    }

    @Override
    public void create(String name, InputStream inputStream) throws IOException {
        // 根据InputStrea创建请求对象
        CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                .index(name)
                .withJson(inputStream));

        elasticsearchClient.indices().create(request);
    }

    @Override
    public void create(String name, Reader reader) throws IOException {
        // 根据Reader创建请求对象
        CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                .index(name)
                .withJson(reader));

        elasticsearchClient.indices().create(request);
    }

    @Override
    public void create(String name, String jsonContent) throws IOException {
        // 根据Reader创建请求对象
        CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                .index(name)
                .withJson(new StringReader(jsonContent)));

        elasticsearchClient.indices().create(request);
    }
}
