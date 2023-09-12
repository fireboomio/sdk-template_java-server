package io.fireboom.plugins;

import io.fireboom.server.hook.MiddlewareHookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public MiddlewareHookResponse handleException(Exception e) {
        log.error(e.getMessage(), e);
        MiddlewareHookResponse response = new MiddlewareHookResponse();
        response.setError(e.getMessage());
        return response;
    }
}
