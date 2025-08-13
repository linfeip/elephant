package com.linfp.elephant.protocol;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.EnumDefinition;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.inject.Guice;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import io.protostuff.compiler.ParserModule;
import io.protostuff.compiler.model.Enum;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DynamicProto {
    private static class InputStreamReader implements FileReader {

        private final InputStream in;

        public InputStreamReader(InputStream in) {
            this.in = in;
        }

        @Override
        public CharStream read(String name) {
            try {
                return CharStreams.fromStream(in);
            } catch (Exception e) {
                log.error("Could not read {}", name, e);
            }
            return null;
        }
    }

    private static class XParserModule extends ParserModule {
        @Override
        protected void configure() {
            bind(Importer.class).to(ImporterImpl.class);
            bind(FileDescriptorLoader.class).to(FileDescriptorLoaderImpl.class);
            bind(ANTLRErrorListener.class).to(ParseErrorLogger.class);
            bind(ANTLRErrorStrategy.class).to(BailErrorStrategy.class);

            Multibinder.newSetBinder(binder(), ProtoContextPostProcessor.class);

            install(new FactoryModuleBuilder()
                    .implement(FileReader.class, MultiPathFileReader.class)
                    .build(FileReaderFactory.class));
        }
    }

    private static class DynamicMethod {
        public String service;
        public String method;
        public String inMsg;
        public String outMsg;
    }

    private final Map<String, DynamicSchema> messages = new ConcurrentHashMap<>();

    private final Map<String, DynamicMethod> methods = new ConcurrentHashMap<>();

    public void register(InputStream protoInputStream) {
        parseProtoFile(protoInputStream);
    }

    public DynamicMessage makeInMessage(String fullMethod, Map<String, Object> fieldValues) {
        var method = methods.get(fullMethod);
        Objects.requireNonNull(method);
        return buildMessage(method.inMsg, fieldValues);
    }

    public Descriptors.Descriptor getInDesc(String fullMethod) {
        var method = methods.get(fullMethod);
        Objects.requireNonNull(method);
        var schema = messages.get(method.inMsg);
        return schema.getMessageDescriptor(method.inMsg);
    }

    public Descriptors.Descriptor getOutDesc(String fullMethod) {
        var method = methods.get(fullMethod);
        Objects.requireNonNull(method);
        var schema = messages.get(method.outMsg);
        return schema.getMessageDescriptor(method.inMsg);
    }

    public DynamicMessage makeOutMessage(String fullMethod, Map<String, Object> fieldValues) {
        var method = methods.get(fullMethod);
        Objects.requireNonNull(method);
        return buildMessage(method.outMsg, fieldValues);
    }

    private DynamicMessage buildMessage(String msg, Map<String, Object> fieldValues) {
        var inMsgSchema = messages.get(msg);
        Objects.requireNonNull(inMsgSchema);

        var builder = inMsgSchema.newMessageBuilder(msg);

        if (fieldValues == null) {
            return builder.build();
        }

        Descriptors.Descriptor msgDesc = builder.getDescriptorForType();

        List<Descriptors.FieldDescriptor> fdList = msgDesc.getFields();

        for (var fd : fdList) {
            String fieldName = fd.getName();
            Object fieldValue = fieldValues.get(fieldName);
            if (fd.isRepeated()) {
                if (fieldValue != null) {
                    List<Object> values = (List<Object>) fieldValue;
                    for (Object ele : values) {
                        Object pbValue = getPBValue(ele, fd);
                        if (null != pbValue) {
                            builder.addRepeatedField(fd, pbValue);
                        }
                    }
                }
            } else {
                Object pbValue = getPBValue(fieldValue, fd);
                if (null != pbValue) {
                    builder.setField(fd, pbValue);
                }
            }
        }

        return builder.build();
    }

    private Object getPBValue(Object fieldValue, Descriptors.FieldDescriptor fd) {
        if (fieldValue == null) {
            return null;
        }
        Descriptors.FieldDescriptor.JavaType javaType = fd.getJavaType();
        switch (javaType) {
            case INT:
                if (fieldValue instanceof Integer) {
                    return fieldValue;
                } else {
                    return Integer.parseInt(String.valueOf(fieldValue));
                }
            case LONG:
                if (fieldValue instanceof Long) {
                    return fieldValue;
                } else {
                    return Long.parseLong(String.valueOf(fieldValue));
                }
            case FLOAT:
                if (fieldValue instanceof Float) {
                    return fieldValue;
                } else {
                    return Float.parseFloat(String.valueOf(fieldValue));
                }
            case DOUBLE:
                if (fieldValue instanceof Double) {
                    return fieldValue;
                } else {
                    return Double.parseDouble(String.valueOf(fieldValue));
                }
            case BOOLEAN:
                if (fieldValue instanceof Boolean) {
                    return fieldValue;
                } else {
                    return Boolean.parseBoolean(String.valueOf(fieldValue));
                }
            case STRING:
                if (fieldValue instanceof String) {
                    return fieldValue;
                } else {
                    return String.valueOf(fieldValue);
                }
            case ENUM:
                return fd.getEnumType().findValueByName(String.valueOf(fieldValue));
            case MESSAGE:
                Map<String, Object> fieldValues = (Map<String, Object>) fieldValue;
                return buildMessage(fd.getMessageType().getFullName(), fieldValues);
            default:
                // BYTE_STRING
                throw new UnsupportedOperationException(javaType.name() + " for " + fd.getName() + " not support yet!");
        }
    }

    private void parseProtoFile(InputStream stream) {
        var injector = Guice.createInjector(new XParserModule());
        var importer = injector.getInstance(Importer.class);

        var context = importer.importFile(
                new InputStreamReader(stream), null);

        var builder = DynamicSchema.newBuilder();

        // build message and enums
        for (var e : context.getProto().getMessages()) {
            MessageDefinition md = createMessageDefinition(e);
            builder.addMessageDefinition(md);
        }

        for (var e : context.getProto().getEnums()) {
            EnumDefinition ed = createEnumDefinition(e);
            builder.addEnumDefinition(ed);
        }

        for (var service : context.getProto().getServices()) {
            for (var method : service.getMethods()) {
                if (method.isArgStream() || method.isReturnStream()) {
                    throw new RuntimeException("not supported service method arg stream or return stream");
                }
                var key = String.format("%s.%s/%s", service.getParent().getPackage(), service.getName(), method.getName());
                var mt = new DynamicMethod();
                mt.method = method.getName();
                mt.service = service.getName();
                mt.inMsg = method.getArgTypeName();
                mt.outMsg = method.getReturnTypeName();
                methods.put(key, mt);
            }
        }

        // build services method
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DynamicSchema schema;
        try {
            schema = builder.build();
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException(e);
        }

        for (var msg : schema.getMessageTypes()) {
            messages.put(msg, schema);
        }
    }

    /**
     * 按照深度优先顺序，构造含有嵌套的MessageDefinition
     */
    private static MessageDefinition createMessageDefinition(Message message) {
        MessageDefinition.Builder builder = MessageDefinition.newBuilder(message.getName());
        for (Field f : message.getFields()) {
            String label = f.isRepeated() ? "repeated" : "optional";
            builder.addField(label, f.getTypeName(), f.getName(), f.getIndex());
        }

        for (Message nestedMessage : message.getMessages()) {
            MessageDefinition nestedMsgDef = createMessageDefinition(nestedMessage);
            builder.addMessageDefinition(nestedMsgDef);
        }

        for (io.protostuff.compiler.model.Enum e : message.getEnums()) {
            EnumDefinition enumDef = createEnumDefinition(e);
            builder.addEnumDefinition(enumDef);
        }

        return builder.build();
    }

    private static EnumDefinition createEnumDefinition(Enum e) {
        EnumDefinition.Builder builder = EnumDefinition.newBuilder(e.getName());
        e.getConstants().forEach(c -> {
            builder.addValue(c.getName(), c.getValue());
        });
        return builder.build();
    }
}
