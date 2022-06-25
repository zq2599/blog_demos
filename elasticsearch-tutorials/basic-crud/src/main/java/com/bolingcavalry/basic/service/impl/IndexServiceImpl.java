package com.bolingcavalry.basic.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.bolingcavalry.basic.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Function;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public void addIndex(String name) throws IOException {
        ApplicationContext applicationContext;
        elasticsearchClient.indices().create(c -> c.index(name));
    }

    @Override
    public boolean indexExists(String name) throws IOException {
        ApplicationContext a;
        return elasticsearchClient.indices().exists(b -> b.index(name)).value();
    }

    @Override
    public void delIndex(String name) throws IOException {
        elasticsearchClient.indices().delete(c -> c.index(name));
    }

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
}
