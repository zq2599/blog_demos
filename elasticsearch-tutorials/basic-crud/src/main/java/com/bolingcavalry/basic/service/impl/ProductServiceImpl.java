package com.bolingcavalry.basic.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.bolingcavalry.basic.bean.Product;
import com.bolingcavalry.basic.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * @program: elasticsearch-tutorials
 * @description: 产品文档相关服务的实现类
 * @author: za2599@gmail.com
 * @create: 2022-06-26 11:18
 **/
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchAsyncClient elasticsearchAsyncClient;

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
    public Product search(String index, String id) throws Exception {

        GetResponse<Product> response = elasticsearchClient.get(g -> g
                .index(index)
                .id(id),
                Product.class
        );

        return response.found() ? response.source() : null;
    }


    public void createAnsync(String index, Product product, BiConsumer<IndexResponse, Exception> action) {
        elasticsearchAsyncClient.index(i -> i
                .index(index)
                .id(product.getId())
                .document(product)
        ).whenComplete(null);
    }
}
