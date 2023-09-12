package io.fireboom.controller;

import graphql.ExecutionResult;
import io.fireboom.plugins.CustomizeHooks;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/{name}/graphql")
    public ExecutionResult query(@PathVariable String name, @RequestBody CustomizeHooks.GraphqlInput graphqlInput, HttpServletResponse response) throws IOException {
        return CustomizeHooks.execute(name, graphqlInput, response);
    }
}
