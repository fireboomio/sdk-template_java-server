package io.fireboom.plugins;


import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import graphql.com.google.common.collect.Maps;
import io.fireboom.server.enums.HookParent;
import io.fireboom.server.enums.MiddlewareHook;
import io.fireboom.server.hook.BaseRequestBody;
import io.fireboom.server.hook.MiddlewareHookResponse;
import io.fireboom.server.hook.RequestHeaders;
import io.fireboom.server.operation.OperationHookPayload;
import io.fireboom.server.operation.OperationHookPayload_response;
import io.fireboom.server.operation.RequestError;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public abstract class OperationHooks<I, O> {

    abstract protected OperationHookBody<I, O> execute(OperationHookBody<I, O> body);

    public static MiddlewareHookResponse handle(MiddlewareHook hook, String path, OperationHookPayload body) throws Exception {
        Function<OperationHookPayload, MiddlewareHookResponse> function = getResolveMap(hook).get(path);
        if (null == function) {
            throw new Exception(StrUtil.format("not found hook [{}] for operation [{}]", hook, path));
        }

        return function.apply(body);
    }

    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> mockResolveMap = Maps.newConcurrentMap();
    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> preResolveMap = Maps.newConcurrentMap();
    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> postResolveMap = Maps.newConcurrentMap();
    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> mutatingPreResolveMap = Maps.newConcurrentMap();
    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> mutatingPostResolveMap = Maps.newConcurrentMap();
    private static final Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> customResolveMap = Maps.newConcurrentMap();

    public static Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> getResolveMap(MiddlewareHook hook) {
        switch (hook) {
            case preResolve:
                return preResolveMap;
            case postResolve:
                return postResolveMap;
            case mutatingPreResolve:
                return mutatingPreResolveMap;
            case mutatingPostResolve:
                return mutatingPostResolveMap;
            case mockResolve:
                return mockResolveMap;
            case customResolve:
                return customResolveMap;
            default:
                return Maps.newConcurrentMap();
        }
    }

    @PostConstruct
    protected void init() {
        Class<?> hookClass = this.getClass();
        MiddlewareHook hook = MiddlewareHook.valueOf(StrUtil.lowerFirst(hookClass.getSimpleName()));
        boolean isMutatingPostResolve = hook.equals(MiddlewareHook.mutatingPostResolve);
        String operationPath = HealthReports.buildRelativeClasspath(hookClass, HookParent.operation, false);
        putOperationResolve(hookClass, operationPath, isMutatingPostResolve, this::execute, getResolveMap(hook));
        log.info("register operation hook [{}] on [{}]", hook, operationPath);
    }


    protected static <I, O> void putOperationResolve(Class<?> hookClass, String operationPath, boolean allowedDataAny,
                                                     Function<OperationHookBody<I, O>, OperationHookBody<I, O>> execute,
                                                     Map<String, Function<OperationHookPayload, MiddlewareHookResponse>> resolveMap) {
        Type[] types = TypeUtil.getTypeArguments(hookClass);
        resolveMap.putIfAbsent(operationPath, body -> {
            OperationHookBody<I, O> input = new OperationHookBody<>();
            input.set__wg(body.get__wg());
            input.input = JSONObject.parseObject(JSON.toJSONString(body.getInput()), types[0]);
            OperationHookPayload_response payloadResponse = body.getResponse();
            OperationBodyResponse<O> response;
            if (null != payloadResponse) {
                response = new OperationBodyResponse<>();
                response.data = JSONObject.parseObject(JSON.toJSONString(payloadResponse.getData()), types[1]);
                input.setResponse(response);
            }
            OperationHookBody<I, O> output = execute.apply(input);
            if (null != payloadResponse && null != (response = output.response)) {
                payloadResponse.setErrors(response.errors);
                payloadResponse.setData(allowedDataAny && response.dataAny != null
                                                ? response.dataAny
                                                : response.data);
            }
            MiddlewareHookResponse hookResponse = new MiddlewareHookResponse();
            hookResponse.setResponse(payloadResponse);
            hookResponse.setInput(output.input);
            hookResponse.setSetClientRequestHeaders(output.setClientRequestHeaders);
            return hookResponse;
        });
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class OperationHookBody<I, O> extends BaseRequestBody {
        private I input;
        private OperationBodyResponse<O> response;
        private RequestHeaders setClientRequestHeaders;
    }

    @Data
    public static class OperationBodyResponse<O> {
        @JSONField(serialize = false)
        private Object dataAny;
        private O data;
        private List<RequestError> errors;
    }
}
