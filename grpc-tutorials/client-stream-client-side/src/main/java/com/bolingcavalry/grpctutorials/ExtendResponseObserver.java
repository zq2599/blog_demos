package com.bolingcavalry.grpctutorials;

import io.grpc.stub.StreamObserver;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 4/24/21 12:01 PM
 * @description 功能介绍
 */
public interface ExtendResponseObserver<T> extends StreamObserver<T> {

    String getExtra();

}
