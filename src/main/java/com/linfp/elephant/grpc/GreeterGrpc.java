package com.linfp.elephant.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 *
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.71.0)",
        comments = "Source: hello.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class GreeterGrpc {

    private GreeterGrpc() {
    }

    public static final java.lang.String SERVICE_NAME = "hello.Greeter";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.linfp.elephant.grpc.HelloRequest,
            com.linfp.elephant.grpc.HelloReply> getSayHelloMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "SayHello",
            requestType = com.linfp.elephant.grpc.HelloRequest.class,
            responseType = com.linfp.elephant.grpc.HelloReply.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.linfp.elephant.grpc.HelloRequest,
            com.linfp.elephant.grpc.HelloReply> getSayHelloMethod() {
        io.grpc.MethodDescriptor<com.linfp.elephant.grpc.HelloRequest, com.linfp.elephant.grpc.HelloReply> getSayHelloMethod;
        if ((getSayHelloMethod = GreeterGrpc.getSayHelloMethod) == null) {
            synchronized (GreeterGrpc.class) {
                if ((getSayHelloMethod = GreeterGrpc.getSayHelloMethod) == null) {
                    GreeterGrpc.getSayHelloMethod = getSayHelloMethod =
                            io.grpc.MethodDescriptor.<com.linfp.elephant.grpc.HelloRequest, com.linfp.elephant.grpc.HelloReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SayHello"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.linfp.elephant.grpc.HelloRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.linfp.elephant.grpc.HelloReply.getDefaultInstance()))
                                    .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("SayHello"))
                                    .build();
                }
            }
        }
        return getSayHelloMethod;
    }

    private static volatile io.grpc.MethodDescriptor<com.linfp.elephant.grpc.PingRequest,
            com.linfp.elephant.grpc.PingReply> getPingMethod;

    @io.grpc.stub.annotations.RpcMethod(
            fullMethodName = SERVICE_NAME + '/' + "Ping",
            requestType = com.linfp.elephant.grpc.PingRequest.class,
            responseType = com.linfp.elephant.grpc.PingReply.class,
            methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<com.linfp.elephant.grpc.PingRequest,
            com.linfp.elephant.grpc.PingReply> getPingMethod() {
        io.grpc.MethodDescriptor<com.linfp.elephant.grpc.PingRequest, com.linfp.elephant.grpc.PingReply> getPingMethod;
        if ((getPingMethod = GreeterGrpc.getPingMethod) == null) {
            synchronized (GreeterGrpc.class) {
                if ((getPingMethod = GreeterGrpc.getPingMethod) == null) {
                    GreeterGrpc.getPingMethod = getPingMethod =
                            io.grpc.MethodDescriptor.<com.linfp.elephant.grpc.PingRequest, com.linfp.elephant.grpc.PingReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Ping"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.linfp.elephant.grpc.PingRequest.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            com.linfp.elephant.grpc.PingReply.getDefaultInstance()))
                                    .setSchemaDescriptor(new GreeterMethodDescriptorSupplier("Ping"))
                                    .build();
                }
            }
        }
        return getPingMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static GreeterStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GreeterStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GreeterStub>() {
                    @java.lang.Override
                    public GreeterStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GreeterStub(channel, callOptions);
                    }
                };
        return GreeterStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports all types of calls on the service
     */
    public static GreeterBlockingV2Stub newBlockingV2Stub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GreeterBlockingV2Stub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GreeterBlockingV2Stub>() {
                    @java.lang.Override
                    public GreeterBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GreeterBlockingV2Stub(channel, callOptions);
                    }
                };
        return GreeterBlockingV2Stub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static GreeterBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GreeterBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GreeterBlockingStub>() {
                    @java.lang.Override
                    public GreeterBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GreeterBlockingStub(channel, callOptions);
                    }
                };
        return GreeterBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static GreeterFutureStub newFutureStub(
            io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<GreeterFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<GreeterFutureStub>() {
                    @java.lang.Override
                    public GreeterFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new GreeterFutureStub(channel, callOptions);
                    }
                };
        return GreeterFutureStub.newStub(factory, channel);
    }

    /**
     *
     */
    public interface AsyncService {

        /**
         *
         */
        default void sayHello(com.linfp.elephant.grpc.HelloRequest request,
                              io.grpc.stub.StreamObserver<com.linfp.elephant.grpc.HelloReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSayHelloMethod(), responseObserver);
        }

        /**
         *
         */
        default void ping(com.linfp.elephant.grpc.PingRequest request,
                          io.grpc.stub.StreamObserver<com.linfp.elephant.grpc.PingReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPingMethod(), responseObserver);
        }
    }

    /**
     * Base class for the server implementation of the service Greeter.
     */
    public static abstract class GreeterImplBase
            implements io.grpc.BindableService, AsyncService {

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return GreeterGrpc.bindService(this);
        }
    }

    /**
     * A stub to allow clients to do asynchronous rpc calls to service Greeter.
     */
    public static final class GreeterStub
            extends io.grpc.stub.AbstractAsyncStub<GreeterStub> {
        private GreeterStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GreeterStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GreeterStub(channel, callOptions);
        }

        /**
         *
         */
        public void sayHello(com.linfp.elephant.grpc.HelloRequest request,
                             io.grpc.stub.StreamObserver<com.linfp.elephant.grpc.HelloReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getSayHelloMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         *
         */
        public void ping(com.linfp.elephant.grpc.PingRequest request,
                         io.grpc.stub.StreamObserver<com.linfp.elephant.grpc.PingReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getPingMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * A stub to allow clients to do synchronous rpc calls to service Greeter.
     */
    public static final class GreeterBlockingV2Stub
            extends io.grpc.stub.AbstractBlockingStub<GreeterBlockingV2Stub> {
        private GreeterBlockingV2Stub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GreeterBlockingV2Stub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GreeterBlockingV2Stub(channel, callOptions);
        }

        /**
         *
         */
        public com.linfp.elephant.grpc.HelloReply sayHello(com.linfp.elephant.grpc.HelloRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getSayHelloMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.linfp.elephant.grpc.PingReply ping(com.linfp.elephant.grpc.PingRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getPingMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do limited synchronous rpc calls to service Greeter.
     */
    public static final class GreeterBlockingStub
            extends io.grpc.stub.AbstractBlockingStub<GreeterBlockingStub> {
        private GreeterBlockingStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GreeterBlockingStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GreeterBlockingStub(channel, callOptions);
        }

        /**
         *
         */
        public com.linfp.elephant.grpc.HelloReply sayHello(com.linfp.elephant.grpc.HelloRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getSayHelloMethod(), getCallOptions(), request);
        }

        /**
         *
         */
        public com.linfp.elephant.grpc.PingReply ping(com.linfp.elephant.grpc.PingRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getPingMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do ListenableFuture-style rpc calls to service Greeter.
     */
    public static final class GreeterFutureStub
            extends io.grpc.stub.AbstractFutureStub<GreeterFutureStub> {
        private GreeterFutureStub(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected GreeterFutureStub build(
                io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new GreeterFutureStub(channel, callOptions);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.linfp.elephant.grpc.HelloReply> sayHello(
                com.linfp.elephant.grpc.HelloRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getSayHelloMethod(), getCallOptions()), request);
        }

        /**
         *
         */
        public com.google.common.util.concurrent.ListenableFuture<com.linfp.elephant.grpc.PingReply> ping(
                com.linfp.elephant.grpc.PingRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getPingMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_SAY_HELLO = 0;
    private static final int METHODID_PING = 1;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final AsyncService serviceImpl;
        private final int methodId;

        MethodHandlers(AsyncService serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_SAY_HELLO:
                    serviceImpl.sayHello((com.linfp.elephant.grpc.HelloRequest) request,
                            (io.grpc.stub.StreamObserver<com.linfp.elephant.grpc.HelloReply>) responseObserver);
                    break;
                case METHODID_PING:
                    serviceImpl.ping((com.linfp.elephant.grpc.PingRequest) request,
                            (io.grpc.stub.StreamObserver<com.linfp.elephant.grpc.PingReply>) responseObserver);
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

    public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
        return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                .addMethod(
                        getSayHelloMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.linfp.elephant.grpc.HelloRequest,
                                        com.linfp.elephant.grpc.HelloReply>(
                                        service, METHODID_SAY_HELLO)))
                .addMethod(
                        getPingMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<
                                        com.linfp.elephant.grpc.PingRequest,
                                        com.linfp.elephant.grpc.PingReply>(
                                        service, METHODID_PING)))
                .build();
    }

    private static abstract class GreeterBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        GreeterBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return com.linfp.elephant.grpc.HelloProto.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("Greeter");
        }
    }

    private static final class GreeterFileDescriptorSupplier
            extends GreeterBaseDescriptorSupplier {
        GreeterFileDescriptorSupplier() {
        }
    }

    private static final class GreeterMethodDescriptorSupplier
            extends GreeterBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final java.lang.String methodName;

        GreeterMethodDescriptorSupplier(java.lang.String methodName) {
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
            synchronized (GreeterGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new GreeterFileDescriptorSupplier())
                            .addMethod(getSayHelloMethod())
                            .addMethod(getPingMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
