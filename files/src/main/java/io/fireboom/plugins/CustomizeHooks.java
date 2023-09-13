package io.fireboom.plugins;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
import graphql.com.google.common.collect.Maps;
import graphql.schema.GraphQLSchema;
import io.fireboom.server.customize.CustomizeHookPayload;
import io.fireboom.server.enums.CustomizeFlag;
import io.fireboom.server.enums.Endpoint;
import io.fireboom.server.enums.HookParent;
import io.fireboom.server.hook.BaseRequestBodyWg;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public abstract class CustomizeHooks {

    abstract protected GraphQLSchema schema();

    private static ThreadLocal<BaseRequestBodyWg> contextVariable = ThreadLocal.withInitial(() -> null);
    private static final Map<String, GraphQL> schemaMap = Maps.newConcurrentMap();
    private static final Map<String, String> helixMap = Maps.newConcurrentMap();

    public static ExecutionResult execute(String name, CustomizeHookPayload body, HttpServletResponse response) throws IOException {
        GraphQL graphQL = schemaMap.get(name);
        if (null == graphQL) {
            throw new RuntimeException("not found graphql: " + name);
        }

        contextVariable.set(body.get__wg());
        ExecutionInput.Builder builder = ExecutionInput.newExecutionInput()
                                                       .query(body.getQuery())
                                                       .operationName(body.getOperationName());
        Optional.ofNullable(body.getVariables()).ifPresent(builder::variables);
        ExecutionInput input = builder.build();
        if (!StrUtil.startWithIgnoreCase(input.getQuery(), CustomizeFlag.subscription.getValue()) || null == response) {
            return graphQL.execute(input);
        }

        CompletableFuture<ExecutionResult> future = graphQL.executeAsync(input);
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.setWriteListener(new WriteListener() {
            @Override
            public void onWritePossible() {
                // 当输出流可写时，将数据写入 SSE 输出流
                if (outputStream.isReady()) {
                    try {
                        ExecutionResult result = future.get();
                        String sseData = "data: " + result.toString() + "\n\n";
                        outputStream.write(sseData.getBytes());
                    } catch (Exception e) {
                        log.error(e.toString(), e);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error(throwable.toString(), throwable);
            }
        });
        return ExecutionResultImpl.newExecutionResult().build();
    }

    public static String html(String name) {
        String html = helixMap.get(name);
        if (null == html) {
            throw new RuntimeException("not found graphql: " + name);
        }

        return html;
    }

    @SneakyThrows
    public static void writeIntrospectJson() {
        String schemaFlag = CustomizeFlag.__schema.getValue();
        CustomizeHookPayload introspectInput = JSONObject.parseObject(introspectJsonString, CustomizeHookPayload.class);
        for (String name : schemaMap.keySet()) {
            ExecutionResult result = execute(name, introspectInput, null);
            if (CollectionUtil.isNotEmpty(result.getErrors())) {
                log.error("introspect [{}] contains errors: {}", name, result.getErrors());
                continue;
            }

            LinkedHashMap<String, Object> data = result.getData();
            String graphqlJsonpath = HealthReports.buildReportFilepath(HookParent.customize, name);
            FileUtil.writeUtf8String(JSON.toJSONString(data.get(schemaFlag)), graphqlJsonpath);
            HealthReports.reportData(HookParent.customize, name);
        }
    }

    private static final String helixTemplate;
    private static final String introspectJsonString;

    static {
        ClassLoader classLoader = CustomizeHooks.class.getClassLoader();
        URL htmlResource = classLoader.getResource("helix.html");
        URL introspectResource = classLoader.getResource("introspect.json");
        Objects.requireNonNull(htmlResource);
        Objects.requireNonNull(introspectResource);

        helixTemplate = FileUtil.readString(htmlResource, Charset.defaultCharset());
        introspectJsonString = FileUtil.readString(introspectResource, Charset.defaultCharset());
    }

    @PostConstruct
    protected void init() {
        String name = this.getClass().getSimpleName();
        schemaMap.put(name, GraphQL.newGraphQL(schema()).build());

        String graphqlEndpoint = StrUtil.replace(Endpoint.customize.getValue(), "{name}", name);
        helixMap.put(name, StrUtil.replace(helixTemplate, CustomizeFlag.graphqlEndpoint.getValue(), graphqlEndpoint));
    }

    protected BaseRequestBodyWg fetchWg() {
        return contextVariable.get();
    }
}
