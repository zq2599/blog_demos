package com.bolingcavalry.grpctutorials.lib;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * gRPC服务，这是个在线商城的购物车服务
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.35.0)",
    comments = "Source: mall.proto")
public final class CartServiceGrpc {

  private CartServiceGrpc() {}

  public static final String SERVICE_NAME = "CartService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.ProductOrder,
      com.bolingcavalry.grpctutorials.lib.AddCartReply> getAddToCartMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddToCart",
      requestType = com.bolingcavalry.grpctutorials.lib.ProductOrder.class,
      responseType = com.bolingcavalry.grpctutorials.lib.AddCartReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.ProductOrder,
      com.bolingcavalry.grpctutorials.lib.AddCartReply> getAddToCartMethod() {
    io.grpc.MethodDescriptor<com.bolingcavalry.grpctutorials.lib.ProductOrder, com.bolingcavalry.grpctutorials.lib.AddCartReply> getAddToCartMethod;
    if ((getAddToCartMethod = CartServiceGrpc.getAddToCartMethod) == null) {
      synchronized (CartServiceGrpc.class) {
        if ((getAddToCartMethod = CartServiceGrpc.getAddToCartMethod) == null) {
          CartServiceGrpc.getAddToCartMethod = getAddToCartMethod =
              io.grpc.MethodDescriptor.<com.bolingcavalry.grpctutorials.lib.ProductOrder, com.bolingcavalry.grpctutorials.lib.AddCartReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddToCart"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.bolingcavalry.grpctutorials.lib.ProductOrder.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.bolingcavalry.grpctutorials.lib.AddCartReply.getDefaultInstance()))
              .setSchemaDescriptor(new CartServiceMethodDescriptorSupplier("AddToCart"))
              .build();
        }
      }
    }
    return getAddToCartMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CartServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CartServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CartServiceStub>() {
        @java.lang.Override
        public CartServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CartServiceStub(channel, callOptions);
        }
      };
    return CartServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CartServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CartServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CartServiceBlockingStub>() {
        @java.lang.Override
        public CartServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CartServiceBlockingStub(channel, callOptions);
        }
      };
    return CartServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CartServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CartServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CartServiceFutureStub>() {
        @java.lang.Override
        public CartServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CartServiceFutureStub(channel, callOptions);
        }
      };
    return CartServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的购物车服务
   * </pre>
   */
  public static abstract class CartServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 客户端流式：添加多个商品到购物车
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.ProductOrder> addToCart(
        io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.AddCartReply> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getAddToCartMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getAddToCartMethod(),
            io.grpc.stub.ServerCalls.asyncClientStreamingCall(
              new MethodHandlers<
                com.bolingcavalry.grpctutorials.lib.ProductOrder,
                com.bolingcavalry.grpctutorials.lib.AddCartReply>(
                  this, METHODID_ADD_TO_CART)))
          .build();
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的购物车服务
   * </pre>
   */
  public static final class CartServiceStub extends io.grpc.stub.AbstractAsyncStub<CartServiceStub> {
    private CartServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CartServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CartServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 客户端流式：添加多个商品到购物车
     * </pre>
     */
    public io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.ProductOrder> addToCart(
        io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.AddCartReply> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getAddToCartMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的购物车服务
   * </pre>
   */
  public static final class CartServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<CartServiceBlockingStub> {
    private CartServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CartServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CartServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * gRPC服务，这是个在线商城的购物车服务
   * </pre>
   */
  public static final class CartServiceFutureStub extends io.grpc.stub.AbstractFutureStub<CartServiceFutureStub> {
    private CartServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CartServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CartServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_ADD_TO_CART = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CartServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CartServiceImplBase serviceImpl, int methodId) {
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
        case METHODID_ADD_TO_CART:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.addToCart(
              (io.grpc.stub.StreamObserver<com.bolingcavalry.grpctutorials.lib.AddCartReply>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CartServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CartServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.bolingcavalry.grpctutorials.lib.MallProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CartService");
    }
  }

  private static final class CartServiceFileDescriptorSupplier
      extends CartServiceBaseDescriptorSupplier {
    CartServiceFileDescriptorSupplier() {}
  }

  private static final class CartServiceMethodDescriptorSupplier
      extends CartServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CartServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (CartServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CartServiceFileDescriptorSupplier())
              .addMethod(getAddToCartMethod())
              .build();
        }
      }
    }
    return result;
  }
}
