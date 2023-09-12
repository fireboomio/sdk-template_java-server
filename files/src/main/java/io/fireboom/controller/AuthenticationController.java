package io.fireboom.controller;

import io.fireboom.plugins.AuthenticationHooks;
import io.fireboom.plugins.GlobalHooks;
import io.fireboom.server.hook.BaseRequestBody;
import io.fireboom.server.hook.MiddlewareHookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

    @PostMapping("/postAuthentication")
    public MiddlewareHookResponse postAuthentication(@RequestBody BaseRequestBody body) {
        AuthenticationHooks.postAuthentication.accept(body.get__wg());
        return GlobalHooks.buildResponse();
    }

    @PostMapping("/mutatingPostAuthentication")
    public MiddlewareHookResponse mutatingPostAuthentication(@RequestBody BaseRequestBody body) {
        return GlobalHooks.buildResponse(AuthenticationHooks.mutatingPostAuthentication.apply(body.get__wg()));
    }

    @PostMapping("/revalidateAuthentication")
    public MiddlewareHookResponse revalidateAuthentication(@RequestBody BaseRequestBody body) {
        return GlobalHooks.buildResponse(AuthenticationHooks.revalidateAuthentication.apply(body.get__wg()));
    }

    @PostMapping("/postLogout")
    public MiddlewareHookResponse postLogout(@RequestBody BaseRequestBody body) {
        AuthenticationHooks.postLogout.accept(body.get__wg());
        return GlobalHooks.buildResponse();
    }
}
