package com.linfp.elephant.grpc;

import io.grpc.stub.StreamObserver;

public class GreeterGrpcService extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello " + request.getName()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingReply> responseObserver) {
        responseObserver.onNext(PingReply.newBuilder().build());
        responseObserver.onCompleted();
    }
}
