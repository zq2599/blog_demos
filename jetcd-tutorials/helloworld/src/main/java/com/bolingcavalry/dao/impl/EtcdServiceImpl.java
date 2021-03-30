package com.bolingcavalry.dao.impl;

import com.bolingcavalry.dao.EtcdService;
import io.etcd.jetcd.Response;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;

/**
 * @Description: etcd服务的实现类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/3/30 8:28
 */
public class EtcdServiceImpl implements EtcdService {
    @Override
    public Response.Header put(String key, String value) {
        return null;
    }

    @Override
    public String getSingle(String key) {
        return null;
    }

    @Override
    public GetResponse getRange(String key, GetOption getOption) {
        return null;
    }

    @Override
    public long deleteSingle(String key) {
        return 0;
    }

    @Override
    public long deleteRange(String key, DeleteOption deleteOption) {
        return 0;
    }
}
