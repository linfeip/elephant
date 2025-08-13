package com.linfp.elephant.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DynamicMessage;
import com.linfp.elephant.api.GrpcCallRequest;
import com.linfp.elephant.api.RunRequest;
import com.linfp.elephant.protocol.DynamicProto;
import com.linfp.elephant.runner.RunnerManager;
import io.grpc.CallOptions;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Map;

@RestController
public class RunnerController {

    @Autowired
    private RunnerManager runnerManager;

    @Autowired
    private ObjectMapper om;

    @PostMapping("saveConfig")
    public String saveConfig() {
        return "OK";
    }

    @PostMapping("run")
    public String run(@Validated @RequestBody RunRequest req) {
        return runnerManager.runAsync(req);
    }

    @PostMapping("stop/{id}")
    public void stop(@PathVariable String id) {
        runnerManager.stop(id);
    }

    @PostMapping("grpcCall")
    public String grpcCall(@Validated @RequestBody GrpcCallRequest req) {
        var dynamicProto = new DynamicProto();
        // 动态解析gRPC proto文件
        for (String proto : req.protos) {
            dynamicProto.register(new ByteArrayInputStream(proto.getBytes()));
        }

        try {
            Map<String, Object> params = om.readValue(req.body, new TypeReference<>() {
            });

            var fullMethod = req.service + "/" + req.method;

            var inArgs = dynamicProto.makeInMessage(fullMethod, params);

            var outDesc = dynamicProto.getOutDesc(fullMethod);

            var inDesc = inArgs.getDescriptorForType();

            var method = MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(fullMethod)
                    .setRequestMarshaller(ProtoUtils.marshaller(
                            DynamicMessage.getDefaultInstance(inDesc)))
                    .setResponseMarshaller(ProtoUtils.marshaller(
                            DynamicMessage.getDefaultInstance(outDesc)))
                    .build();

            var channel = ManagedChannelBuilder
                    .forTarget(req.addr)
                    .usePlaintext()
                    .build();

            var resp = ClientCalls.blockingUnaryCall(
                    channel, method, CallOptions.DEFAULT, inArgs);
            return resp.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
