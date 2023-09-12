package io.fireboom.plugins;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.fireboom.server.enums.HookParent;
import io.fireboom.server.enums.UploadHook;
import io.fireboom.server.hook.BaseRequestBody;
import io.fireboom.server.hook.MiddlewareHookResponse;
import io.fireboom.server.storage.HookFile;
import io.fireboom.server.storage.UploadHookPayload;
import io.fireboom.server.storage.UploadHookPayload_error;
import io.fireboom.server.storage.UploadHookResponse;
import graphql.com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public abstract class UploadProfileHooks<M> {
    abstract protected UploadHookResponse execute(UploadProfileHooks.UploadBody<M> body);

    public static MiddlewareHookResponse handle(String provider, String profile, UploadHook hook, UploadHookPayload body) throws Exception {
        String upload = StrUtil.join(StrUtil.SLASH, provider, profile);
        Function<UploadHookPayload, UploadHookResponse> function = getResolveMap(hook).get(upload);
        if (null == function) {
            throw new Exception(StrUtil.format("not found hook [{}] for upload [{}]", hook, upload));
        }

        return GlobalHooks.buildResponse(function.apply(body));
    }

    private static final Map<String, Function<UploadHookPayload, UploadHookResponse>> preUploadMap = Maps.newConcurrentMap();
    private static final Map<String, Function<UploadHookPayload, UploadHookResponse>> postUploadMap = Maps.newConcurrentMap();

    public static Map<String, Function<UploadHookPayload, UploadHookResponse>> getResolveMap(UploadHook hook) {
        switch (hook) {
            case preUpload:
                return preUploadMap;
            case postUpload:
                return postUploadMap;
            default:
                return Maps.newConcurrentMap();
        }
    }

    @PostConstruct
    protected void init() {
        Class<?> hookClass = this.getClass();
        UploadHook hook = UploadHook.valueOf(StrUtil.lowerFirst(hookClass.getSimpleName()));
        Map<String, Function<UploadHookPayload, UploadHookResponse>> resolveMap = getResolveMap(hook);
        String uploadHookPath = HealthReports.buildRelativeClasspath(hookClass, HookParent.storage, false);
        int packageDepth = StrUtil.count(uploadHookPath, StrUtil.SLASH);
        if (packageDepth != 1) {
            log.error("upload hook package expect depth 2, but found [{}]", packageDepth + 1);
            return;
        }

        Type[] types = TypeUtil.getTypeArguments(hookClass);
        resolveMap.putIfAbsent(uploadHookPath, body -> {
            UploadProfileHooks.UploadBody<M> input = new UploadBody<>();
            input.set__wg(body.get__wg());
            input.setFile(body.getFile());
            input.setError(body.getError());
            input.meta = JSONObject.parseObject(JSON.toJSONString(body.getMeta()), types[0]);
            return this.execute(input);
        });
        log.info("register upload hook [{}] on [{}]", hook, uploadHookPath);
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public static class UploadBody<M> extends BaseRequestBody {
        private UploadHookPayload_error error;
        private HookFile file;
        private M meta;
    }
}
