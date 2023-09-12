package io.fireboom.controller;


import io.fireboom.plugins.FunctionHooks;
import io.fireboom.server.hook.MiddlewareHookResponse;
import io.fireboom.server.operation.OperationHookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/function")
public class FunctionController {

    @PostMapping("/{path}")
    public MiddlewareHookResponse execute(@PathVariable String path, @RequestBody OperationHookPayload body) throws Exception {
        return FunctionHooks.handle(path, body);
    }
}
