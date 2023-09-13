package io.fireboom.plugins;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONPath;
import graphql.com.google.common.collect.Maps;
import io.fireboom.server.enums.HookParent;
import io.fireboom.server.enums.OperationField;
import io.fireboom.server.enums.OperationType;
import io.fireboom.server.hook.MiddlewareHookResponse;
import io.fireboom.server.operation.OperationHookPayload;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.swagger.v3.oas.models.Components.COMPONENTS_SCHEMAS_REF;

@Slf4j
public abstract class FunctionHooks<I, O> {

    protected OperationType operationType() {
        return OperationType.QUERY;
    }

    abstract protected OperationHooks.OperationHookBody<I, O> execute(OperationHooks.OperationHookBody<I, O> body);

    public static MiddlewareHookResponse handle(String path, OperationHookPayload body) throws Exception {
        Function<OperationHookPayload, MiddlewareHookResponse> function = functionMap.get(path);
        if (null == function) {
            throw new Exception(StrUtil.format("not found function hook [{}]", path));
        }

        return function.apply(body);
    }

    private static final String definitionsPrefix = "#/definitions/";
    private static final String definitionsProperty = "definitions";
    private static final String jsonEmpty = "{}";
    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> functionMap = Maps.newConcurrentMap();

    @PostConstruct
    protected void init() {
        Class<?> hookClass = this.getClass();
        String operationPath = HealthReports.buildRelativeClasspath(hookClass, HookParent.function, true);
        OperationHooks.putOperationResolve(this.getClass(), operationPath, true, this::execute, functionMap);
        log.info("register function hook [{}]", operationPath);

        Type[] types = TypeUtil.getTypeArguments(hookClass);
        rewriteOperationJsonString(HookParent.function, operationPath, new HashMap<OperationField, Object>() {{
            put(OperationField.operationType, operationType().getValue());
            put(OperationField.variablesSchema, buildJsonSchemaString(types[0]));
            put(OperationField.responseSchema, buildJsonSchemaString(types[1]));
        }});
    }

    protected static void rewriteOperationJsonString(HookParent parent, String operationPath, Map<OperationField, Object> resetMap) {
        String operationJsonpath = HealthReports.buildReportFilepath(parent, operationPath);
        String operationJsonStr;
        try {
            operationJsonStr = FileUtil.readUtf8String(operationJsonpath);
        } catch (IORuntimeException ignored) {
            operationJsonStr = jsonEmpty;
        }
        if (MapUtil.isNotEmpty(resetMap)) {
            for (Map.Entry<OperationField, Object> entry : resetMap.entrySet()) {
                operationJsonStr = JSONPath.set(operationJsonStr, entry.getKey().getValue(), entry.getValue());
            }
        }
        operationJsonStr = JSONPath.set(operationJsonStr, OperationField.path.getValue(), operationPath);
        FileUtil.writeUtf8String(operationJsonStr, operationJsonpath);
        HealthReports.reportData(parent, operationPath);
    }

    private static String buildJsonSchemaString(Type type) {
        // 返回的UnmodifiableMap未实现remove方法
        Map<String, Schema> scheamMap = ModelConverters.getInstance().readAll(type);
        String typeName = StrUtil.subAfter(type.getTypeName(), StrUtil.DOT, true);
        String finalTypeName = StrUtil.subAfter(typeName, "$", true);
        String schemaString = JSON.toJSONString(scheamMap.get(finalTypeName));
        scheamMap = scheamMap.keySet().stream().filter(x -> !x.equals(finalTypeName)).collect(Collectors.toMap(x -> x, scheamMap::get));
        if (!scheamMap.isEmpty()) {
            schemaString = JSONPath.set(schemaString, definitionsProperty, scheamMap);
        }
        return StrUtil.replace(schemaString, COMPONENTS_SCHEMAS_REF, definitionsPrefix);
    }
}
