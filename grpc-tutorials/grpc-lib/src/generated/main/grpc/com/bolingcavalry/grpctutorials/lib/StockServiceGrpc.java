package com.bolingcavalry.grpctutorials.lib;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * gRPC服务，这是个在线商城的库存服务
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.35.0)",
    comments = "Source: mall.proto")
public final class StockServiceGrpc {

  private StockServiceGrpc() {}

  public static final String SERVICE_NAME = "StockService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.ProductOrder,
      com.bolingcavalry.grpctutorials.lib.DeductReply> getBatchDeductMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchDeduct",
      requestType = com.bolingcavalry.grpctutorials.lib.ProductOrder.class,
      responseType = com.bolingcavalry.grpctutorials.lib.DeductReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.ProductOrder,
      com.bolingcavalry.grpctutorials.lib.DeductReply> getBatchDeductMethod() {
    io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.ProductOrder, com.bolingcavalry.grpctutorials.lib.DeductReply> getBatchDeductMethod;
    if ((getBatchDeductMethod = StockServiceGrpc.getBatchDeductMethod) == null) {
      synchronized (StockServiceGrpc.class) {
        if ((getBatchDeductMethod = StockServiceGrpc.getBatchDeductMethod) == null) {
          StockServiceGrpc.getBatchDeductMethod = getBatchDeductMethod =
              io.grpc.MethodDescriptor.<com.bolingcavalry.grpctutorials.lib.ProductOrder, com.bolingcavalry.grpctutorials.lib.DeductReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BatchDeduct"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.bolingcavalry.grpctutorials.lib.ProductOrder.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.bolingcavalry.grpctutorials.lib.DeductReply.getDefaultInstance()))
              .setSchemaDescriptor(new StockServiceMethodDescriptorSupplier("BatchDeduct"))
              .build();
        }
      }
    }
    return getBatchDeductMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StockServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StockServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StockServiceStub>() {
        @java.lang.Override
        public StockServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StockServiceStub(channel, callOptions);
        }
      };
    return StockServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StockServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StockServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StockServiceBlockingStub>() {
        @java.lang.Override
        public StockServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StockServiceBlockingStub(channel, callOptions);
        }
      };
    return StockServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StockServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StockServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StockServiceFutureStub>() {
        @java.lang.Override
        public StockServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StockServiceFutureStub(channel, callOptions);
        }
      };
    return StockServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的库存服务
   * </pre>
   */
  public static abstract class StockServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 双向流式：批量扣减库存
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.ProductOrder> batchDeduct(
        io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.DeductReply> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getBatchDeductMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getBatchDeductMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                com.bolingcavalry.grpctutorials.lib.ProductOrder,
                com.bolingcavalry.grpctutorials.lib.DeductReply>(
                  this, METHODID_BATCH_DEDUCT)))
          .build();
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的库存服务
   * </pre>
   */
  public static final class StockServiceStub extends io.grpc.stub.AbstractAsyncStub<StockServiceStub> {
    private StockServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StockServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StockServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 双向流式：批量扣减库存
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.ProductOrder> batchDeduct(
        io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.DeductReply> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getBatchDeductMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的库存服务
   * </pre>
   */
  public static final class StockServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<StockServiceBlockingStub> {
    private StockServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StockServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StockServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的库存服务
   * </pre>
   */
  public static final class StockServiceFutureStub extends io.grpc.stub.AbstractFutureStub<StockServiceFutureStub> {
    private StockServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StockServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StockServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_BATCH_DEDUCT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final StockServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(StockServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_BATCH_DEDUCT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.batchDeduct(
              (io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.DeductReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class StockServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StockServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.bolingcavalry.grpctutorials.lib.MallProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StockService");
    }
  }

  private static final class StockServiceFileDescriptorSupplier
      extends StockServiceBaseDescriptorSupplier {
    StockServiceFileDescriptorSupplier() {}
  }

  private static final class StockServiceMethodDescriptorSupplier
      extends StockServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    StockServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (StockServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StockServiceFileDescriptorSupplier())
              .addMethod(getBatchDeductMethod())
              .build();
        }
      }
    }
    return result;
  }
}
