package com.bolingcavalry.fromjson.service.impl;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.bolingcavalry.fromjson.service.EsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EsServiceImplTest {

    @Autowired
    EsService esService;

    @Test
    void create() throws Exception {
        // 索引名
        String indexName = "product002";

        // 构建setting时，builder用到的lambda
        Function<IndexSettings.Builder, ObjectBuilder<IndexSettings>> settingFn = sBuilder -> sBuilder
                .index(iBuilder -> iBuilder
                        // 三个分片
                        .numberOfShards("3")
                        // 一个副本
                        .numberOfReplicas("1")
                );

        // 新的索引有三个字段，每个字段都有自己的property，这里依次创建
        Property keywordProperty = Property.of(pBuilder -> pBuilder.keyword(kBuilder -> kBuilder.ignoreAbove(256)));
        Property textProperty = Property.of(pBuilder -> pBuilder.text(tBuilder -> tBuilder));
        Property integerProperty = Property.of(pBuilder -> pBuilder.integer(iBuilder -> iBuilder));

        // // 构建mapping时，builder用到的lambda
        Function<TypeMapping.Builder, ObjectBuilder<TypeMapping>> mappingFn = mBuilder -> mBuilder
                .properties("name", keywordProperty)
                .properties("description", textProperty)
                .properties("price", integerProperty);

        // 创建索引，并且指定了setting和mapping
        esService.create(indexName, settingFn, mappingFn);

    }

    @Test
    void createByInputStream() throws Exception {
        // 文件名
        String filePath = "/Users/will/temp/202206/25/product003.json";
        // 索引名
        String indexName = "product003";
        // 通过InputStream创建索引
        esService.create(indexName, new FileInputStream(filePath));
    }

    @Test
    void createByReader() throws Exception {
        // 文件名
        String filePath = "/Users/zhaoqin/temp/202206/25/product003.json";
        // 索引名
        String indexName = "product004";

        // 通过InputStream创建索引
        esService.create(indexName, new FileReader(filePath));
    }

    @Test
    void createByString() throws Exception {
        // 文件名
        String jsonContent = "{\n" +
                "  \"settings\": {\n" +
                "    \"number_of_shards\": 3,\n" +
                "    \"number_of_replicas\": 1\n" +
                "  },\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"name\": {\n" +
                "        \"type\": \"keyword\",\n" +
                "        \"ignore_above\": 256\n" +
                "      },\n" +
                "      \"description\": {\n" +
                "        \"type\": \"text\"\n" +
                "      },\n" +
                "      \"price\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        // 索引名
        String indexName = "product005";

        // 通过InputStream创建索引
        esService.create(indexName, jsonContent);
    }
}