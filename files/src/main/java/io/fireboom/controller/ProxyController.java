package io.fireboom.controller;

import io.fireboom.plugins.ProxyHooks;
import io.fireboom.server.global.OnRequestHookPayload;
import io.fireboom.server.hook.MiddlewareHookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/proxy")
public class ProxyController {

    @PostMapping("/{path}")
    public MiddlewareHookResponse execute(@PathVariable String path, @RequestBody OnRequestHookPayload body) throws Exception {
        return ProxyHooks.handle(path, body);
    }
}