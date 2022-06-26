package com.bolingcavalry.basic.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.bolingcavalry.basic.bean.Product;
import com.bolingcavalry.basic.service.ProductService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @program: elasticsearch-tutorials
 * @description: 产品文档相关服务的实现类
 * @author: za2599@gmail.com
 * @create: 2022-06-26 11:18
 **/
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchAsyncClient elasticsearchAsyncClient;

    @Override
    public Product search(String index, String id) {

        GetResponse<Product> response = null;

        try {
            response = elasticsearchClient.get(g -> g
                            .index(index)
                            .id(id),
                    Product.class);
        } catch (Exception exception) {
            log.error("query [" + index + "] by id [" + id + "] error", exception);
        }

        return response.found() ? response.source() : null;
    }

    @Override
    public IndexResponse createByFluentDSL(String index, Product product) throws Exception {
        return elasticsearchClient.index(i -> i
                .index(index)
                .id(product.getId())
                .document(product)
        );
    }

    @Override
    public IndexResponse createByBuilderPattern(String index, Product product) throws Exception {
        IndexRequest.Builder<Product> indexReqBuilder = new IndexRequest.Builder<>();

        indexReqBuilder.index(index);
        indexReqBuilder.id(product.getId());
        indexReqBuilder.document(product);

        return elasticsearchClient.index(indexReqBuilder.build());
    }

    @Override
    public void createAnsync(String index, Product product, BiConsumer<IndexResponse, Throwable> action) {
        elasticsearchAsyncClient.index(i -> i
                .index(index)
                .id(product.getId())
                .document(product)
        ).whenComplete(action);
    }

    @Override
    public IndexResponse createByJSON(String index, String id, String jsonContent) throws Exception {
        return elasticsearchClient.index(i -> i
                .index(index)
                .id(id)
                .withJson(new StringReader(jsonContent))
        );
    }

    @Override
    public BulkResponse bulkCreate(String index, List<Product> products) throws Exception {
        BulkRequest.Builder br = new BulkRequest.Builder();

        // 将每一个product对象都放入builder中
        products.stream()
                .forEach(product -> br
                        .operations(op -> op
                                .index(idx -> idx
                                        .index(index)
                                        .id(product.getId())
                                        .document(product))));

        return elasticsearchClient.bulk(br.build());
    }

    @Override
    public BulkResponse bulkDelete(String index, List<String> docIds) throws Exception {
        BulkRequest.Builder br = new BulkRequest.Builder();

        // 将每一个product对象都放入builder中
        docIds.stream()
                .forEach(id -> br
                        .operations(op -> op
                                .delete(d -> d
                                        .index(index)
                                        .id(id))));

        return elasticsearchClient.bulk(br.build());
    }

    @Override
    public ObjectNode getObjectNode(String index, String id) throws Exception {
        GetResponse<ObjectNode> response = elasticsearchClient.get(g -> g
                                          .index(index)
                                          .id(id),
                                          ObjectNode.class);

        return response.found() ? response.source() : null;
    }
}
