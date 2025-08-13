package com.linfp.elephant;

import com.google.protobuf.DynamicMessage;
import com.linfp.elephant.grpc.GreeterGrpc;
import com.linfp.elephant.grpc.HelloRequest;
import com.linfp.elephant.protocol.DynamicProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.HashMap;

public class GreeterGrpcClientTests {

    @Test
    void testGreeterGrpc() throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50000)
                .usePlaintext()
                .build();

        var stub = GreeterGrpc.newBlockingStub(channel);

        var reply = stub.sayHello(
                HelloRequest.newBuilder().setName("World").build()
        );

        System.out.println(reply.getMessage());

        channel.shutdownNow();
    }

    @Test
    void testDynamicProto() throws Exception {

        var filepath = Paths.get("src/main/proto/hello.proto");

        var helloReqM = new HashMap<String, Object>();
        helloReqM.put("name", "World");

        var param = new HashMap<String, Object>();
        param.put("arg1", "11");
        param.put("arg2", 22);

        helloReqM.put("param", param);
        helloReqM.put("num", 1);

        var dp = new DynamicProto();

        dp.register(new FileInputStream(filepath.toFile()));

        var fullMethod = "hello.Greeter/SayHello";
        var msg = dp.makeInMessage(fullMethod, helloReqM);
        var data = msg.toByteArray();

        var inDesc = msg.getDescriptorForType();
        var outDesc = dp.getOutDesc(fullMethod);

        var helloX = HelloRequest.parseFrom(data);

        var method = MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(fullMethod)
                .setRequestMarshaller(ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(inDesc)))
                .setResponseMarshaller(ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(outDesc)))
                .build();

        var channel = ManagedChannelBuilder
                .forTarget("localhost:50000")
                .usePlaintext()
                .build();
        try {
            DynamicMessage resp = ClientCalls.blockingUnaryCall(
                    channel, method, io.grpc.CallOptions.DEFAULT, msg);
            System.out.println(resp);
        } finally {
            channel.shutdownNow();
        }
    }
}
