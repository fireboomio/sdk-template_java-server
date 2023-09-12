package io.fireboom.controller;

import io.fireboom.plugins.OperationHooks;
import io.fireboom.server.enums.MiddlewareHook;
import io.fireboom.server.hook.MiddlewareHookResponse;
import io.fireboom.server.operation.OperationHookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/operation")
public class OperationController {

    @PostMapping("/{path}/mockResolve")
    public MiddlewareHookResponse mockResolve(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return OperationHooks.handle(MiddlewareHook.mockResolve, path, body);
    }

    @PostMapping("/{path}/preResolve")
    public MiddlewareHookResponse preResolve(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return OperationHooks.handle(MiddlewareHook.preResolve, path, body);
    }

    @PostMapping("/{path}/postResolve")
    public MiddlewareHookResponse postResolve(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return OperationHooks.handle(MiddlewareHook.postResolve, path, body);
    }

    @PostMapping("/{path}/mutatingPreResolve")
    public MiddlewareHookResponse mutatingPreResolve(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return OperationHooks.handle(MiddlewareHook.mutatingPreResolve, path, body);
    }

    @PostMapping("/{path}/mutatingPostResolve")
    public MiddlewareHookResponse mutatingPostResolve(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return OperationHooks.handle(MiddlewareHook.mutatingPostResolve, path, body);
    }

    @PostMapping("/{path}/customResolve")
    public MiddlewareHookResponse customResolve(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return OperationHooks.handle(MiddlewareHook.customResolve, path, body);
    }
}
