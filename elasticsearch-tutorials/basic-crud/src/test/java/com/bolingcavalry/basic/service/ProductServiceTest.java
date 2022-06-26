package com.bolingcavalry.basic.service;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.bolingcavalry.basic.bean.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;

@Slf4j
@SpringBootTest
class ProductServiceTest {

    private final static String INDEX_NAME = "product006";

    @Autowired
    IndexService indexService;

    @Autowired
    ProductService productService;

    @BeforeEach
    void init() throws Exception {
        // 如果索引存在，就删掉
        if (indexService.indexExists(INDEX_NAME)) {
            indexService.delIndex(INDEX_NAME);
        }

        // 创建索引，并且设置setting和mapping
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
        indexService.create(INDEX_NAME, settingFn, mappingFn);
    }

    @Test
    void createByFluentDSL() throws Exception {
        String id = "1";
        String name = "name-1";
        String desc = "description-1";
        int price = 101;

        Product product = new Product(id, name, desc, price);

        // 新增一个文档
        productService.create(INDEX_NAME, product);

        // 根据id从ES查找记录
        Product queryRlt = productService.search(INDEX_NAME, id);

        log.info("query result : {}", queryRlt);

        // 验证是否符合预期
        Assertions.assertNotNull(queryRlt);
        Assertions.assertEquals(id, queryRlt.getId());
        Assertions.assertEquals(name, queryRlt.getName());
        Assertions.assertEquals(desc, queryRlt.getDescription());
        Assertions.assertEquals(price, queryRlt.getPrice());
    }
}