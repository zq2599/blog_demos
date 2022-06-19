package com.bolingcavalry.basic.service;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;

import java.io.IOException;
import java.util.function.Function;

public interface IndexService {

    /**
     * 新建指定名称的索引
     * @param name
     * @throws IOException
     */
    void addIndex(String name) throws IOException;

    /**
     * 检查制定名称的索引是否存在
     * @param name
     * @return
     * @throws IOException
     */
    boolean indexExists(String name) throws IOException;

    /**
     * 删除指定索引
     * @param name
     * @throws IOException
     */
    void delIndex(String name) throws IOException;

    /**
     * 创建索引，指定setting和mapping
     * @param name 索引名称
     * @param settingFn 索引参数
     * @param mappingFn 索引结构
     * @throws IOException
     */
    void create(String name,
                Function<IndexSettings.Builder, ObjectBuilder<IndexSettings>> settingFn,
                Function<TypeMapping.Builder, ObjectBuilder<TypeMapping>> mappingFn) throws IOException;
}
