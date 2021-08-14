package com.bolingcavalry.dao.impl;

import com.bolingcavalry.dao.AdvancedEtcdService;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.PutOption;
import io.grpc.stub.CallStreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @Description: Etcd高级操作的服务接口实现
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/4/4 8:23
 */
@Slf4j
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
    public void putWithLease(String key, String value) throws Exception {

        Lease leaseClient = getClient().getLeaseClient();

        leaseClient.grant(60)
                .thenAccept(result -> {

                    // 租约ID
                    long leaseId = result.getID();

                    log.info("[{}]申请租约成功，租约ID [{}]", key, Long.toHexString(leaseId));

                    // 准备好put操作的client
                    KV kvClient = getClient().getKVClient();

                    // put操作时的可选项，在这里指定租约ID
                    PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();

                    // put操作
                    kvClient.put(bytesOf(key), bytesOf(value), putOption)
                            .thenAccept(putResponse -> {
                                // put操作完成后，再设置无限续租的操作
                                leaseClient.keepAlive(leaseId, new CallStreamObserver<LeaseKeepAliveResponse>() {
                                    @Override
                                    public boolean isReady() {
                                        return false;
                                    }

                                    @Override
                                    public void setOnReadyHandler(Runnable onReadyHandler) {

                                    }

                                    @Override
                                    public void disableAutoInboundFlowControl() {

                                    }

                                    @Override
                                    public void request(int count) {
                                    }

                                    @Override
                                    public void setMessageCompression(boolean enable) {

                                    }

                                    /**
                                     * 每次续租操作完成后，该方法都会被调用
                                     * @param value
                                     */
                                    @Override
                                    public void onNext(LeaseKeepAliveResponse value) {
                                        log.info("[{}]续租完成，TTL[{}]", Long.toHexString(leaseId), value.getTTL());
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        log.error("onError", t);
                                    }

                                    @Override
                                    public void onCompleted() {
                                        log.info("onCompleted");
                                    }
                                });
                            });
                });
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
