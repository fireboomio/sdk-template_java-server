package io.fireboom.plugins;


import cn.hutool.core.util.ArrayUtil;
import io.fireboom.server.enums.MiddlewareHook;
import io.fireboom.server.global.*;
import io.fireboom.server.hook.MiddlewareHookResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.function.Function;

@Slf4j
public class GlobalHooks {

    public static Function<OnRequestHookPayload, OnRequestHookResponse> beforeOriginRequest = body -> {
        throw new RuntimeException("404 not found");
    };
    public static Function<OnRequestHookPayload, OnRequestHookResponse> onOriginRequest = body -> {
        throw new RuntimeException("404 not found");
    };
    public static Function<OnResponseHookPayload, OnResponseHookResponse> onOriginResponse = body -> {
        throw new RuntimeException("404 not found");
    };
    public static Function<OnWsConnectionInitHookPayload, OnWsConnectionInitHookResponse> onConnectionInit = body -> {
        throw new RuntimeException("404 not found");
    };

    public static MiddlewareHookResponse buildResponse(Object... data) {
        MiddlewareHookResponse response = new MiddlewareHookResponse();
        if (ArrayUtil.isNotEmpty(data)) {
            response.setResponse(data[0]);
        }
        return response;
    }

    @PostConstruct
    protected void init() {
        Class<?>[] interfaces = this.getClass().getInterfaces();
        if (ArrayUtil.contains(interfaces, BeforeOriginRequest.class)) {
            beforeOriginRequest = ((BeforeOriginRequest) this)::execute;
            log.info("register httpTransport hook [{}]", MiddlewareHook.beforeOriginRequest);
            return;
        }

        if (ArrayUtil.contains(interfaces, OnOriginRequest.class)) {
            onOriginRequest = ((OnOriginRequest) this)::execute;
            log.info("register httpTransport hook [{}]", MiddlewareHook.onOriginRequest);
            return;
        }

        if (ArrayUtil.contains(interfaces, OnOriginResponse.class)) {
            onOriginResponse = ((OnOriginResponse) this)::execute;
            log.info("register httpTransport hook [{}]", MiddlewareHook.onOriginResponse);
            return;
        }

        if (ArrayUtil.contains(interfaces, OnConnectionInit.class)) {
            onConnectionInit = ((OnConnectionInit) this)::execute;
            log.info("register httpTransport hook [{}]", MiddlewareHook.onConnectionInit);
        }
    }

    public interface BeforeOriginRequest {
        OnRequestHookResponse execute(OnRequestHookPayload body);
    }

    public interface OnOriginRequest {
        OnRequestHookResponse execute(OnRequestHookPayload body);
    }

    public interface OnOriginResponse {
        OnResponseHookResponse execute(OnResponseHookPayload body);
    }

    public interface OnConnectionInit {
        OnWsConnectionInitHookResponse execute(OnWsConnectionInitHookPayload body);
    }
}
