package com.linfp.elephant.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DynamicMessage;
import com.linfp.elephant.metrics.Metrics;
import io.grpc.CallOptions;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;

import java.time.Duration;
import java.util.Map;

public class GrpcCallAction implements IAction {
    private final ActionData data;

    private final GrpcArgs grpcArgs;

    private final ObjectMapper om;

    public GrpcCallAction(ActionData data, ObjectMapper om) {
        this.data = data;
        try {
            this.grpcArgs = om.readValue(data.data, GrpcCallAction.GrpcArgs.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.om = om;
    }

    @Override
    public Metrics.Result doAction(Robot robot) throws InterruptedException {
        if (data.delay != null && !data.delay.isZero()) {
            Thread.sleep(data.delay);
        }

        var result = new Metrics.Result();
        var fullMethod = grpcArgs.service + "/" + grpcArgs.method;

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

            var channel = robot.getRunner().getChannel(grpcArgs.addr);
            if (channel == null) {
                var ss = grpcArgs.addr.split(":", 2);
                if (ss.length != 2) {
                    throw new RuntimeException("Invalid grpc address: " + grpcArgs.addr);
                }
                var name = ss[0];
                var port = Integer.parseInt(ss[1]);
                channel = ManagedChannelBuilder
                        .forAddress(name, port)
                        .usePlaintext()
                        .build();
                robot.getRunner().setChannel(grpcArgs.addr, channel);
            }

            var resp = ClientCalls.blockingUnaryCall(
                    channel, method, CallOptions.DEFAULT, inArgs);

        } catch (Exception e) {
            result.code = 1;
            result.error = e.getMessage();
        } finally {
            result.end = System.nanoTime();
            result.name = "grpc.call";
            result.comment = data.comment;
        }

        return result;
    }

    @Override
    public int step() {
        return data.step;
    }

    @Override
    public ActionData getData() {
        return data;
    }

    public static class GrpcArgs {
        public String addr;
        public String service;
        public String method;
        public String body;
    }
}
