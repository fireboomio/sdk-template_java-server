package io.fireboom.controller;

import io.fireboom.plugins.GlobalHooks;
import io.fireboom.server.global.OnRequestHookPayload;
import io.fireboom.server.global.OnResponseHookPayload;
import io.fireboom.server.global.OnWsConnectionInitHookPayload;
import io.fireboom.server.hook.MiddlewareHookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/global")
public class GlobalController {

    @RequestMapping("/httpTransport/beforeOriginRequest")
    public MiddlewareHookResponse beforeOriginRequest(@RequestBody OnRequestHookPayload body) {
        return GlobalHooks.buildResponse(GlobalHooks.beforeOriginRequest.apply(body));
    }

    @RequestMapping("/httpTransport/onOriginRequest")
    public MiddlewareHookResponse onOriginRequest(@RequestBody OnRequestHookPayload body) {
        return GlobalHooks.buildResponse(GlobalHooks.onOriginRequest.apply(body));
    }

    @RequestMapping("/httpTransport/onOriginResponse")
    public MiddlewareHookResponse onOriginResponse(@RequestBody OnResponseHookPayload body) {
        return GlobalHooks.buildResponse(GlobalHooks.onOriginResponse.apply(body));
    }

    @RequestMapping("/wsTransport/onConnectionInit")
    public MiddlewareHookResponse onConnectionInit(@RequestBody OnWsConnectionInitHookPayload body) {
        return GlobalHooks.buildResponse(GlobalHooks.onConnectionInit.apply(body));
    }
}
