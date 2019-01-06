package com.hengyi.japp.mes.auto.interfaces.jikon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2018-06-22
 */
public class JikonUtil {
    public static final int SUCCESS_CODE = 100000;
    public static final String SUCCESS_MESSAGE = "操作成功";

    public static final int ERROR_CODE = 100013;

    public static String success(Object data) {
        return getResponse(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static String error(Throwable ex) {
        return getResponse(ERROR_CODE, ex.getMessage(), null);
    }

    @SneakyThrows
    public static String getResponse(int code, String message, Object data) {
        ObjectNode result = MAPPER.createObjectNode();
        JsonNode meta = MAPPER.createObjectNode()
                .put("code", code)
                .put("message", message);
        result.set("meta", meta);
        if (data != null) {
            result.set("data", MAPPER.convertValue(data, JsonNode.class));
        } else {
            result.set("data", NullNode.getInstance());
        }
        return MAPPER.writeValueAsString(result);
    }

    @SneakyThrows
    public static String ok() {
        ObjectNode result = MAPPER.createObjectNode();
        JsonNode meta = MAPPER.createObjectNode()
                .put("code", SUCCESS_CODE)
                .put("message", SUCCESS_MESSAGE);
        result.set("meta", meta);
        result.put("data", "OK");
        return MAPPER.writeValueAsString(result);
    }

}
