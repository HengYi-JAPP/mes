package com.hengyi.japp.mes.auto;

import com.hengyi.japp.mes.auto.domain.DyeingResult;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static io.vertx.config.yaml.YamlProcessor.YAML_MAPPER;
import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2018-06-20
 */
@Slf4j
public class Util {
    /**
     * hrId 正则测试
     * 当前规则： 1 到 9 数字开头，后面 7 位数字，共8位数字
     */
    public static final Pattern hrIdP = Pattern.compile("^[1-9]\\d{7}$");

    public static boolean isHr(String s) {
        return StringUtils.isBlank(s) ? false : hrIdP.matcher(s).matches();
    }


    public static JsonObject encode(RoutingContext rc) {
        final JsonObject principal = Optional.ofNullable(rc.user()).map(User::principal).orElse(null);
        final Map<String, String> pathParams = rc.pathParams();
        final Map<String, List<String>> queryParams = rc.queryParams().names().parallelStream().collect(toMap(Function.identity(), rc.queryParams()::getAll));
        return new JsonObject().put("principal", principal)
                .put("pathParams", pathParams)
                .put("queryParams", queryParams)
                .put("body", rc.getBodyAsString());
    }

    @SneakyThrows
    public static JsonObject readJsonObject(String first, String... more) {
        return readJsonObject(Paths.get(first, more));
    }

    @SneakyThrows
    public static JsonObject readJsonObject(Path path) {
        final String extension = FilenameUtils.getExtension(path.toString());
        switch (StringUtils.lowerCase(extension)) {
            case "json": {
                final Map map = MAPPER.readValue(path.toFile(), Map.class);
                return new JsonObject(map);
            }
            case "yaml":
            case "yml": {
                final Map map = YAML_MAPPER.readValue(path.toFile(), Map.class);
                return new JsonObject(map);
            }
        }
        throw new RuntimeException(path + "，格式不支持！");
    }

    public static Class<?> parameterizedType(PropertyDescriptor propertyDescriptor) {
        return (Class<?>) Optional.ofNullable(propertyDescriptor)
                .map(PropertyDescriptor::getReadMethod)
                .map(Method::getGenericReturnType)
                .map(it -> (ParameterizedType) it)
                .map(ParameterizedType::getActualTypeArguments)
                .map(it -> it[0])
                .orElse(null);
    }

    public static String getDyeingExceptionString(DyeingResult dyeingResult) {
        if (dyeingResult.isHasException()) {
            switch (dyeingResult.getDyeingPrepare().getType()) {
                case FIRST:
                    return "正常抽样染判异常";
                case CROSS_LINEMACHINE_LINEMACHINE:
                    return "交叉织袜,位与位染判异常";
                case CROSS_LINEMACHINE_SPINDLE:
                    return "交叉织袜,位与锭染判异常";
                case SECOND:
                    return "二次织袜染判异常";
                case THIRD:
                    return "三次织袜染判异常";
            }
        }
        return null;
    }

}
