package grpctutorials;

import io.grpc.stub.StreamObserver;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 4/29/21 08:29 AM
 * @description 扩展接口
 */
public interface ExtendResponseObserver<T> extends StreamObserver<T> {

    String getExtra();

}
