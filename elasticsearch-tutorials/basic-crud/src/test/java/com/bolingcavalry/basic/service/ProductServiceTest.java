package com.bolingcavalry.basic.service;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.bolingcavalry.basic.bean.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Function;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductServiceTest {

    private final static String INDEX_NAME = "product006";

    @Autowired
    IndexService indexService;

    @Autowired
    ProductService productService;

    /**
     * 是否已经执行过初始化操作的标志
     */
    private static boolean isInited = false;

    /**
     * 根据指定文档ID从ES查询数据，并逐字段检查是否符合预期
     * @param id
     * @param name
     * @param desc
     * @param price
     */
    private void check(String id, String name, String desc, int price) {
        // 根据文档id从ES查找文档
        Product product = null;

        try {
            product = productService.search(INDEX_NAME, id);
        } catch (Exception exception) {
            log.error("search document [" + id + "] error", exception);
        }

        log.info("query result : {}", product);

        // 逐个字段检查
        Assertions.assertNotNull(product);
        Assertions.assertEquals(id, product.getId());
        Assertions.assertEquals(name, product.getName());
        Assertions.assertEquals(desc, product.getDescription());
        Assertions.assertEquals(price, product.getPrice());
    }

    @BeforeEach
    void init() throws Exception {
        // 初始化操作只要一次就行了
        if(isInited) {
            return;
        }

        isInited = true;

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
    @Order(1)
    @DisplayName("fluent style新建单个文档")
    void createByFluentDSL() throws Exception {
        String id = "1";
        String name = "name-1";
        String desc = "description-1";
        int price = 101;

        // 新增一个文档
        productService.createByFluentDSL(INDEX_NAME, new Product(id, name, desc, price));

        // 验证是否符合预期
        check(id, name, desc, price);
    }

    @Test
    @Order(2)
    @DisplayName("builder pattern新建单个文档")
    void createByBuilderPattern() throws Exception {
        String id = "2";
        String name = "name-2";
        String desc = "description-2";
        int price = 102;

        // 新增一个文档
        productService.createByBuilderPattern(INDEX_NAME, new Product(id, name, desc, price));

        // 验证是否符合预期
        check(id, name, desc, price);
    }

    @Test
    @Order(3)
    @DisplayName("用JSON字符串新建单个文档")
    void createByJSON() throws Exception {
        String id = "3";
        String name = "name-3";
        String desc = "description-3";
        int price = 103;

        // 新增一个文档
        productService.createByFluentDSL(INDEX_NAME, new Product(id, name, desc, price));

        // 验证是否符合预期
        check(id, name, desc, price);
    }

    @Test
    @Order(4)
    @DisplayName("异步新建单个文档")
    void createAnsync() throws Exception {
        String id = "4";
        // 原始JSON，注意，里面的单引号在JSON中不合法，这里为了提高可读性，先用单引号，后面替换成双引号
        String jsonContent = "{'id':'4','name':'name-4','description':'description-4','price':104}";

        // 将字符串中的单引号替换成双引号，才是正常的JSON
        jsonContent = jsonContent.replace('\'', '"');

        // 新增一个文档
        productService.createByJSON(INDEX_NAME, id, jsonContent);

        // 验证是否符合预期
        check(id, "name-4", "description-4", 104);
    }
}