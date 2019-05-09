package com.kennyzhu.micro.framework.protobuf;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ProtobufUtil   
 *  * @package    com.kennyzhu.micro.framework.protobuf  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:00  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
import com.google.gson.*;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import com.kennyzhu.micro.framework.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ProtobufUtil {
    public static final int MAX_HEADER_CHUNK_SIZE = 1000;
    public static final int MAX_BODY_CHUNK_SIZE = 10_000_000;
    private static final Logger logger = LoggerFactory.getLogger(ProtobufUtil.class);

    private static <TYPE extends Message> TYPE.Builder getBuilder(Class<TYPE> messageClass) throws NoSuchMethodException,
            InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException {

        Constructor<TYPE> constructor = messageClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        TYPE instance = constructor.newInstance();

        return instance.newBuilderForType();
    }

    /**
     * NOTE: this is only using the first element of the JsonArray
     */
    public static <TYPE extends Message> TYPE jsonToProtobuf(JsonArray request, Class<TYPE> messageClass) {
        if (request == null || request.size() < 1 || request.get(0).isJsonNull()) {
            return null;
        }
        return jsonToProtobuf(request.get(0).toString(), messageClass);
    }

    /**
     * Converts a JSON String to a protobuf message
     * <p>
     * Note: Ignores unknown fields
     *
     * @param input        the input String to convert
     * @param messageClass the protobuf message class to convert into
     * @return the converted protobuf message (null in case of null input)
     */
    public static <TYPE extends Message> TYPE jsonToProtobuf(String input, Class<TYPE> messageClass) {
        if (input == null) {
            return null;
        }

        if (!isValidJSON(input)) {
            try {
                return (TYPE) getBuilder(messageClass).getDefaultInstanceForType();
            } catch (Exception e) {
                logger.warn("Error building protobuf object of type {} from json: {}",
                        messageClass.getName(), input);
            }
        }

        try {
            TYPE.Builder builder = getBuilder(messageClass);
            JsonElement element = new JsonParser().parse(input);
            cleanJsonElement(element);
            JsonFormat.parser().ignoringUnknownFields().merge(element.toString(), builder);
            return (TYPE) builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing json to protobuf. Input = " + input, e);
        }
    }

    private static void cleanJsonElement(JsonElement element) {
        if (element.isJsonNull() || element.isJsonPrimitive()) {
            return;
        }
        if (element.isJsonArray()) {
            cleanJsonArray(element.getAsJsonArray());
        }
        if (element.isJsonObject()) {
            cleanJsonObject(element.getAsJsonObject());
        }
    }

    private static void cleanJsonArray(JsonArray array) {
        Iterator<JsonElement> iter = array.iterator();
        while (iter.hasNext()) {
            JsonElement ele = iter.next();
            if (ele.isJsonNull()) {
                iter.remove();
                continue;
            } else {
                cleanJsonElement(ele);
            }
        }
    }

    private static void cleanJsonObject(JsonObject element) {
        Set<Map.Entry<String, JsonElement>> members = element.entrySet();
        Iterator<Map.Entry<String, JsonElement>> iter = members.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, JsonElement> member = iter.next();
            JsonElement value = member.getValue();
            cleanJsonElement(value);
        }
    }

    private static boolean isValidJSON(String input) {
        if (StringUtils.isBlank(input)) {
            logger.warn("Parsing empty json string to protobuf is deprecated and will be removed in " +
                    "the next major release");
            return false;
        }

        if (!input.startsWith("{")) {
            logger.warn("Parsing json string that does not start with { is deprecated and will be " +
                    "removed in the next major release");
            return false;
        }

        try {
            new JsonParser().parse(input);
        } catch (JsonParseException ex) {
            return false;
        }

        return true;
    }

    /**
     * Converts a byte array to a protobuf message
     *
     * @param data         the byte array to convert
     * @param messageClass the protobuf message class to convert into
     * @return the converted protobuf message
     * @throws RpcCallException if something goes wrong during the deserialization
     */
    public static <TYPE extends Message> TYPE byteArrayToProtobuf(byte data[], Class<TYPE> messageClass)
            throws RpcCallException {
        try {
            Message.Builder builder = getBuilder(messageClass);
            return (TYPE) builder.mergeFrom(data).build();
        } catch (Exception e) {
            throw new RpcCallException(RpcCallException.Category.InternalServerError,
                    "Error deserializing byte array to protobuf: " + e);
        }
    }

    /**
     * Creates an empty protobuf message of the specified type
     *
     * @param klass the protobuf message type
     * @return the generated protobuf message
     */
    public static <TYPE extends Message> TYPE newEmptyMessage(Class<TYPE> klass) {
        try {
            Message.Builder builder = getBuilder(klass);
            return (TYPE) builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing byte array to protobuf", e);
        }
    }

    /**
     * Converts a protobuf message to a JSON object
     * <p>
     * Note: Preserves the field names as defined in the proto definition
     *
     * @param input the protobuf message to convert
     * @return the converted JSON object
     */
    public static JsonObject protobufToJson(Message input) {
        JsonObject object = new JsonObject();
        if (input == null) {
            logger.warn("Protobuf message was null");
        } else {
            try {
                String jsonString = JsonFormat.printer()
                        .preservingProtoFieldNames()
                        .print(input);
                object = new JsonParser().parse(jsonString).getAsJsonObject();
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing protobuf to json", e);
            }
        }
        return object;
    }

    /**
     * Converts a protobuf message to a JSON object
     * <p>
     * Note: Camel-cases the field names as defined in the proto definition
     *
     * @param input the protobuf message to convert
     * @return the converted JSON object
     */
    public static JsonObject protobufToJsonCamelCase(Message input) {
        JsonObject object = new JsonObject();
        if (input == null) {
            logger.warn("Protobuf message was null");
        } else {
            try {
                String jsonString = JsonFormat.printer()
                        .print(input);
                object = new JsonParser().parse(jsonString).getAsJsonObject();
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing protobuf to json", e);
            }
        }
        return object;
    }

    /**
     * Converts a protobuf message to a JSON object
     * <p>
     * Note: Preserves the field names as defined in the proto definition
     * Note:
     *
     * @param input the protobuf message to convert
     * @return the converted JSON object
     */
    public static JsonObject protobufToJsonWithDefaultValues(Message input) {
        JsonObject object = new JsonObject();
        if (input == null) {
            logger.warn("Protobuf message was null");
        } else {
            try {
                String jsonString = JsonFormat.printer()
                        .preservingProtoFieldNames()
                        .includingDefaultValueFields()
                        .print(input);
                object = new JsonParser().parse(jsonString).getAsJsonObject();
            } catch (Exception e) {
                throw new RuntimeException("Error deserializing protobuf to json", e);
            }
        }
        return object;
    }

    /**
     * Converts a JSON object to a protobuf message.
     * <p>
     * Note: Ignores unknown fields
     *
     * @param builder the proto message type builder
     * @param input   the JSON object to convert
     * @return the converted protobuf message
     */
    public static Message fromJson(Message.Builder builder, JsonObject input) throws Exception {
        JsonFormat.parser().ignoringUnknownFields().merge(input.toString(), builder);
        return builder.build();
    }

    /**
     * Converts a proto file name into a class name according to the rules defined by protobuf:
     * https://developers.google.com/protocol-buffers/docs/reference/java-generated
     *
     * The file name will be camel cased (and underscores, hyphens etc. stripped out).
     * @param protoFileName The file name to process: e.g. my_service.proto
     * @return The class name: MyService
     */
    public static String toClassName(String protoFileName) {

        if (protoFileName == null) {
            return null;
        }
        String fileName = FileUtil.stripPath(protoFileName);
        fileName = FileUtil.stripExtension(fileName);

        String parts[] = fileName.split("[^A-Za-z0-9]");

        StringBuilder classNameBuilder = new StringBuilder();
        for (String part : parts) {
            classNameBuilder.append(StringUtils.capitalize(part));
        }
        return classNameBuilder.toString();
    }
}
