package com.bolingcavalry.fromjson.service;

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.util.ObjectBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Function;

public interface EsService {

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

    /**
     * 以InputStream为入参创建索引
     * @param name 索引名称
     * @param inputStream 包含JSON内容的文件流对象
     */
    void create(String name, InputStream inputStream) throws IOException;

    /**
     * 以Reader为入参创建索引
     * @param name 索引名称
     * @param reader 包含JSON内容的文件流对象
     */
    void create(String name, Reader reader) throws IOException;

    /**
     * 以字符串为入参创建索引
     * @param name 索引名称
     * @param jsonContent 包含JSON内容的字符串
     */
    void create(String name, String jsonContent) throws IOException;
}
