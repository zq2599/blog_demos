package com.bolingcavalry.basic.service;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;
import com.bolingcavalry.basic.bean.Product;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * 为了方便单元测试，用此方法辅助创建Product对象
     * @param id
     * @return
     */
    private static Product create(int id) {
        return new Product(String.valueOf(id), "name-"+id, "description-"+id, 1000+id);
    }

    /**
     * 根据指定文档ID从ES查询数据，并逐字段检查是否符合预期
     * @param id
     */
    private void check(int id) {
        // 根据文档id从ES查找文档
        Product product = null;

        try {
            product = productService.search(INDEX_NAME, String.valueOf(id));
        } catch (Exception exception) {
            log.error("search document [" + id + "] error", exception);
        }

        log.info("query result : {}", product);

        // 逐个字段检查
        Assertions.assertNotNull(product);
        Assertions.assertEquals(String.valueOf(id), product.getId());
        Assertions.assertEquals("name-"+id, product.getName());
        Assertions.assertEquals("description-"+id, product.getDescription());
        Assertions.assertEquals(1000+id, product.getPrice());
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
                .properties("id", keywordProperty)
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
        int id = 1;

        // 新增一个文档
        productService.createByFluentDSL(INDEX_NAME, create(id));

        // 验证是否符合预期
        check(id);
    }

    @Test
    @Order(2)
    @DisplayName("builder pattern新建单个文档")
    void createByBuilderPattern() throws Exception {
        int id = 2;

        // 新增一个文档
        productService.createByBuilderPattern(INDEX_NAME, create(id));

        // 验证是否符合预期
        check(id);
    }

    @Test
    @Order(3)
    @DisplayName("用JSON字符串新建单个文档")
    void createByJSON() throws Exception {
        int id = 3;

        // 原始JSON，注意，里面的单引号在JSON中不合法，这里为了提高可读性，先用单引号，后面替换成双引号
        String jsonContent = "{'id':'3','name':'name-3','description':'description-3','price':1003}";
        // 将字符串中的单引号替换成双引号，才是正常的JSON
        jsonContent = jsonContent.replace('\'', '"');

        // 新增一个文档
        productService.createByJSON(INDEX_NAME, String.valueOf(id), jsonContent);

        // 验证是否符合预期
        check(id);
    }

    @Test
    @Order(4)
    @DisplayName("异步新建单个文档")
    void createAnsync() throws Exception {
        int id = 4;

        productService.createAnsync(INDEX_NAME, create(id), new BiConsumer<>() {
            @Override
            public void accept(IndexResponse indexResponse, Throwable throwable) {
                // throwable必须为空
                Assertions.assertNull(throwable);
                // 验证结果
                check(id);
            }
        });
    }

    @Test
    @Order(5)
    @DisplayName("批量新建文档")
    void bulkCreate() throws Exception {
        int start = 5;
        int end = 10;

        // 构造产品集合
        List<Product> list = IntStream
                            .range(start, end)
                            .mapToObj(i -> create(i))
                            .collect(Collectors.toList());

        // 批量新增
        productService.bulkCreate(INDEX_NAME, list);

        // 逐个检查
        IntStream.range(start, end).forEach(i -> check(i));

        // 检查完毕后，全部删掉
        List<String> docIds = IntStream
                              .range(start, end)
                              .mapToObj(String::valueOf)
                              .collect(Collectors.toList());

        // 批量删除
        productService.bulkDelete(INDEX_NAME, docIds);

        // 再逐个检查，确保每一个文档都不存在
        IntStream.range(start, end)
                 .forEach(i -> Assertions.assertNull(productService.search(INDEX_NAME, String.valueOf(i))));
    }

    @Test
    @Order(6)
    @DisplayName("返回ObjectNode类型")
    void getObjectNode() throws Exception {
        int id = 11;

        productService.createByFluentDSL(INDEX_NAME, create(id));

        // 查找返回的是ObjectNode对象，这样更通用
        ObjectNode objectNode = productService.getObjectNode(INDEX_NAME, String.valueOf(id));

        Assertions.assertNotNull(objectNode);
        Assertions.assertEquals(String.valueOf(id), objectNode.get("id").asText());
        Assertions.assertEquals("name-" + id, objectNode.get("name").asText());
        Assertions.assertEquals("description-"+id, objectNode.get("description").asText());
        Assertions.assertEquals(1000+id, objectNode.get("price").asInt());
    }

}