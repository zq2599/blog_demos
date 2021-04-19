package com.bolingcavalry.grpctutorials.lib;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * gRPC服务，这是个在线商城的订单服务
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.35.0)",
    comments = "Source: mall.proto")
public final class MallOrderGrpc {

  private MallOrderGrpc() {}

  public static final String SERVICE_NAME = "MallOrder";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.Buyer,
      com.bolingcavalry.grpctutorials.lib.Order> getListOrdersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListOrders",
      requestType = com.bolingcavalry.grpctutorials.lib.Buyer.class,
      responseType = com.bolingcavalry.grpctutorials.lib.Order.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.Buyer,
      com.bolingcavalry.grpctutorials.lib.Order> getListOrdersMethod() {
    io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.Buyer, com.bolingcavalry.grpctutorials.lib.Order> getListOrdersMethod;
    if ((getListOrdersMethod = MallOrderGrpc.getListOrdersMethod) == null) {
      synchronized (MallOrderGrpc.class) {
        if ((getListOrdersMethod = MallOrderGrpc.getListOrdersMethod) == null) {
          MallOrderGrpc.getListOrdersMethod = getListOrdersMethod =
              io.grpc.MethodDescriptor.<com.bolingcavalry.grpctutorials.lib.Buyer, com.bolingcavalry.grpctutorials.lib.Order>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListOrders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.bolingcavalry.grpctutorials.lib.Buyer.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.bolingcavalry.grpctutorials.lib.Order.getDefaultInstance()))
              .setSchemaDescriptor(new MallOrderMethodDescriptorSupplier("ListOrders"))
              .build();
        }
      }
    }
    return getListOrdersMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MallOrderStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MallOrderStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MallOrderStub>() {
        @java.lang.Override
        public MallOrderStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MallOrderStub(channel, callOptions);
        }
      };
    return MallOrderStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MallOrderBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MallOrderBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MallOrderBlockingStub>() {
        @java.lang.Override
        public MallOrderBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MallOrderBlockingStub(channel, callOptions);
        }
      };
    return MallOrderBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MallOrderFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MallOrderFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MallOrderFutureStub>() {
        @java.lang.Override
        public MallOrderFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MallOrderFutureStub(channel, callOptions);
        }
      };
    return MallOrderFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的订单服务
   * </pre>
   */
  public static abstract class MallOrderImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 服务端流式：订单列表接口，入参是买家信息，返回订单列表(用stream修饰返回值)
     * </pre>
     */
    public void listOrders(com.bolingcavalry.grpctutorials.lib.Buyer request,
        io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.Order> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListOrdersMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getListOrdersMethod(),
            io.grpc.stub.ServerCalls.asyncServerStreamingCall(
              new MethodHandlers<
                com.bolingcavalry.grpctutorials.lib.Buyer,
                com.bolingcavalry.grpctutorials.lib.Order>(
                  this, METHODID_LIST_ORDERS)))
          .build();
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的订单服务
   * </pre>
   */
  public static final class MallOrderStub extends io.grpc.stub.AbstractAsyncStub<MallOrderStub> {
    private MallOrderStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MallOrderStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MallOrderStub(channel, callOptions);
    }

    /**
     * <pre>
     * 服务端流式：订单列表接口，入参是买家信息，返回订单列表(用stream修饰返回值)
     * </pre>
     */
    public void listOrders(com.bolingcavalry.grpctutorials.lib.Buyer request,
        io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.Order> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getListOrdersMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的订单服务
   * </pre>
   */
  public static final class MallOrderBlockingStub extends io.grpc.stub.AbstractBlockingStub<MallOrderBlockingStub> {
    private MallOrderBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MallOrderBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MallOrderBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 服务端流式：订单列表接口，入参是买家信息，返回订单列表(用stream修饰返回值)
     * </pre>
     */
    public java.util.Iterator<com.bolingcavalry.grpctutorials.lib.Order> listOrders(
        com.bolingcavalry.grpctutorials.lib.Buyer request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getListOrdersMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的订单服务
   * </pre>
   */
  public static final class MallOrderFutureStub extends io.grpc.stub.AbstractFutureStub<MallOrderFutureStub> {
    private MallOrderFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MallOrderFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MallOrderFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_LIST_ORDERS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final MallOrderImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(MallOrderImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_ORDERS:
          serviceImpl.listOrders((com.bolingcavalry.grpctutorials.lib.Buyer) request,
              (io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.Order>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class MallOrderBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MallOrderBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.bolingcavalry.grpctutorials.lib.MallProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MallOrder");
    }
  }

  private static final class MallOrderFileDescriptorSupplier
      extends MallOrderBaseDescriptorSupplier {
    MallOrderFileDescriptorSupplier() {}
  }

  private static final class MallOrderMethodDescriptorSupplier
      extends MallOrderBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MallOrderMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MallOrderGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MallOrderFileDescriptorSupplier())
              .addMethod(getListOrdersMethod())
              .build();
        }
      }
    }
    return result;
  }
}
