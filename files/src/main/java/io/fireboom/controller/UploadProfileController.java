package io.fireboom.controller;

import io.fireboom.plugins.UploadProfileHooks;
import io.fireboom.server.enums.UploadHook;
import io.fireboom.server.hook.MiddlewareHookResponse;
import io.fireboom.server.storage.UploadHookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadProfileController {

    @PostMapping("/{provider}/{profile}/preUpload")
    public MiddlewareHookResponse preUpload(@PathVariable String provider, @PathVariable String profile, @RequestBody UploadHookPayload body) throws Exception {
        return UploadProfileHooks.handle(provider, profile, UploadHook.preUpload, body);
    }

    @PostMapping("/{provider}/{profile}/postUpload")
    public MiddlewareHookResponse postUpload(@PathVariable String provider, @PathVariable String profile, @RequestBody UploadHookPayload body) throws Exception {
        return UploadProfileHooks.handle(provider, profile, UploadHook.postUpload, body);
    }
}