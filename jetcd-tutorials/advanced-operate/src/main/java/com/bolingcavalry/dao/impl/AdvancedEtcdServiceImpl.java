package com.bolingcavalry.dao.impl;

import com.bolingcavalry.dao.AdvancedEtcdService;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.PutOption;
import org.apache.commons.collections.CollectionUtils;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @Description: Etcd高级操作的服务接口实现
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/4/4 8:23
 */
public class AdvancedEtcdServiceImpl implements AdvancedEtcdService {

    private Client client;

    private Object lock = new Object();

    private String endpoints;

    public AdvancedEtcdServiceImpl(String endpoints) {
        super();
        this.endpoints = endpoints;
    }

    /**
     * 将字符串转为客户端所需的ByteSequence实例
     * @param val
     * @return
     */
    public static ByteSequence bytesOf(String val) {
        return ByteSequence.from(val, UTF_8);
    }



    private Client getClient() {
        if (null==client) {
            synchronized (lock) {
                if (null==client) {
                    client = Client.builder().endpoints(endpoints.split(",")).build();
                }
            }
        }

        return client;
    }

    @Override
    public boolean cas(String key, String expectValue, String updateValue) throws Exception {
        // 将三个String型的入参全部转成ByteSequence类型
        ByteSequence bsKey = bytesOf(key);
        ByteSequence bsExpectValue = bytesOf(expectValue);
        ByteSequence bsUpdateValue = bytesOf(updateValue);

        // 是否相等的比较
        Cmp cmp = new Cmp(bsKey, Cmp.Op.EQUAL, CmpTarget.value(bsExpectValue));

        // 执行事务
        TxnResponse txnResponse = getClient().getKVClient()
                                .txn()
                                .If(cmp)
                                .Then(Op.put(bsKey, bsUpdateValue, PutOption.DEFAULT))
                                .commit()
                                .get();

        // 如果操作成功，isSucceeded方法会返回true，并且PutResponse也有内容
        return txnResponse.isSucceeded() && CollectionUtils.isNotEmpty(txnResponse.getPutResponses());
    }


    @Override
    public Watch.Watcher watch(String key, Watch.Listener listener) throws Exception {
        return getClient().getWatchClient().watch(bytesOf(key), listener);
    }

    @Override
    public void close() {
        getClient().close();
        client = null;
    }


}
