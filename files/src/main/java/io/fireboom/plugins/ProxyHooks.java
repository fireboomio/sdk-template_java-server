package io.fireboom.plugins;


import cn.hutool.core.util.StrUtil;
import io.fireboom.server.enums.HookParent;
import io.fireboom.server.global.OnRequestHookPayload;
import io.fireboom.server.global.OnResponseHookResponse;
import io.fireboom.server.hook.MiddlewareHookResponse;
import graphql.com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public abstract class ProxyHooks {

    abstract protected OnResponseHookResponse execute(OnRequestHookPayload body);

    public static MiddlewareHookResponse handle(String path, OnRequestHookPayload body) throws Exception {
        Function<OnRequestHookPayload, OnResponseHookResponse> function = proxyMap.get(path);
        if (null == function) {
            throw new Exception(StrUtil.format("not found proxy hook [{}]", path));
        }

        return GlobalHooks.buildResponse(function.apply(body));
    }

    private static final Map<String, Function<OnRequestHookPayload, OnResponseHookResponse>> proxyMap = Maps.newConcurrentMap();

    @PostConstruct
    protected void init() {
        String operationPath = HealthReports.buildRelativeClasspath(this.getClass(), HookParent.proxy, true);
        proxyMap.put(operationPath, this::execute);
        log.info("register proxy hook [{}]", operationPath);

        FunctionHooks.rewriteOperationJsonString(HookParent.proxy, operationPath, null);
    }
}
