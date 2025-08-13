package com.linfp.elephant.robot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DynamicMessage;
import com.linfp.elephant.metrics.Metrics;
import io.grpc.CallOptions;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

public class GrpcCallAction implements IAction {
    private final ActionData data;

    private final GrpcArgs grpcArgs;

    private final ObjectMapper om;

    public GrpcCallAction(ActionData data, ObjectMapper om) {
        this.data = data;
        try {
            this.grpcArgs = om.readValue(data.getData(), GrpcCallAction.GrpcArgs.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.om = om;
    }

    @Override
    public Metrics.Result doAction(Robot robot) throws InterruptedException {
        if (data.getDelay() != null && !data.getDelay().isZero()) {
            Thread.sleep(data.getDelay());
        }

        var result = new Metrics.Result();
        var start = System.nanoTime();

        var fullMethod = grpcArgs.getService() + "/" + grpcArgs.getMethod();

        try {
            Map<String, Object> params = om.readValue(grpcArgs.body, new TypeReference<>() {
            });

            var inArgs = robot.getDynamicProto().makeInMessage(fullMethod, params);

            var outDesc = robot.getDynamicProto().getOutDesc(fullMethod);
            var inDesc = inArgs.getDescriptorForType();

            var method = MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(fullMethod)
                    .setRequestMarshaller(ProtoUtils.marshaller(
                            DynamicMessage.getDefaultInstance(inDesc)))
                    .setResponseMarshaller(ProtoUtils.marshaller(
                            DynamicMessage.getDefaultInstance(outDesc)))
                    .build();

            // TODO 这里的channel可以缓存起来放在, Runner下的global cache中
            var channel = ManagedChannelBuilder
                    .forTarget(grpcArgs.getAddr())
                    .usePlaintext()
                    .build();

            var resp = ClientCalls.blockingUnaryCall(
                    channel, method, CallOptions.DEFAULT, inArgs);

        } catch (Exception e) {
            result.setCode(1);
            result.setError(e.getMessage());
        } finally {
            var elapsed = System.nanoTime() - start;
            result.setElapsed(Duration.ofNanos(elapsed));
            result.setName("grpc.call");
            result.setComment(data.getComment());
        }

        return result;
    }

    @Override
    public int step() {
        return data.getStep();
    }

    @Override
    public ActionData getData() {
        return data;
    }

    @Data
    public static class GrpcArgs {
        private String addr;
        private String service;
        private String method;
        private String body;
    }
}
