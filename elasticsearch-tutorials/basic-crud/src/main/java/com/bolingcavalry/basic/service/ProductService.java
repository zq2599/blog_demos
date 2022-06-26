package com.bolingcavalry.basic.service;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.bolingcavalry.basic.bean.Product;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * @program: elasticsearch-tutorials
 * @description: 文档相关服务的接口
 * @author: za2599@gmail.com
 * @create: 2022-06-26 11:11
 **/
public interface ProductService {

    /**
     * 根据文档id查找文档
     * @param index
     * @param id
     * @return
     * @throws Exception
     */
    Product search(String index, String id);

    /**
     * 新增一个文档
     * @param index 索引名
     * @param product 文档对象
     * @return
     */
    IndexResponse createByFluentDSL(String index, Product product) throws Exception;

    /**
     * 新增一个文档
     * @param index 索引名
     * @param product 文档对象
     * @return
     */
    IndexResponse createByBuilderPattern(String index, Product product) throws Exception;

    /**
     * 异步新增文档
     * @param index
     * @param product
     * @param action
     */
    void createAnsync(String index, Product product, BiConsumer<IndexResponse, Throwable> action);

    /**
     * 用JSON字符串创建文档
     * @param index
     * @param id
     * @param jsonContent
     * @return
     */
    IndexResponse createByJSON(String index, String id, String jsonContent) throws Exception;

    /**
     * 批量增加文档
     * @param index 索引
     * @param products 要增加的对象集合
     * @return 批量操作的结果
     * @throws Exception
     */
    BulkResponse bulkCreate(String index, List<Product> products) throws Exception;

    /**
     * 批量删除文档
     * @param index 索引
     * @param docIds 要删除的文档id集合
     * @return
     * @throws Exception
     */
    BulkResponse bulkDelete(String index, List<String> docIds) throws Exception;

    /**
     * 在指定索引中查找指定id的文档，返回类型是ObjectNode
     * @param index 索引
     * @param id 文档id
     * @return ObjectNode类型的查找结果
     */
    ObjectNode getObjectNode(String index, String id) throws Exception ;
}
