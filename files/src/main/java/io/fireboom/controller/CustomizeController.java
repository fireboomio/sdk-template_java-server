package io.fireboom.controller;

import com.alibaba.fastjson.JSONObject;
import graphql.ExecutionResult;
import io.fireboom.plugins.CustomizeHooks;
import io.fireboom.server.customize.CustomizeHookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/gqls")
public class CustomizeController {

    @GetMapping("/{name}/graphql")
    public String playground(@PathVariable String name) {
        return CustomizeHooks.html(name);
    }

    @PostMapping(value = "/{name}/graphql", consumes = {MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ExecutionResult query(@PathVariable String name, @RequestBody byte[] data, HttpServletResponse response) throws IOException {
        CustomizeHookPayload body = JSONObject.parseObject(data, CustomizeHookPayload.class);
        return CustomizeHooks.execute(name, body, response);
    }
}
