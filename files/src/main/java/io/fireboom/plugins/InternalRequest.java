package io.fireboom.plugins;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.fireboom.server.enums.Endpoint;
import io.fireboom.server.hook.BaseRequestBodyWg;
import io.fireboom.server.hook.WunderGraphRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.Optional;

public class InternalRequest {

    private static final int timeout = 10;

    public static String internalBaseURL;

    public static <I, O> O execute(Meta<I, O> meta, I input, BaseRequestBodyWg... originWg) {
        String requestURI = StrUtil.replace(Endpoint._internalRequest.getValue(), "{path}", meta.path);
        WunderGraphRequest request = new WunderGraphRequest();
        request.setRequestURI(requestURI);
        request.setMethod(HttpMethod.POST.name());
        BaseRequestBodyWg wg = new BaseRequestBodyWg();
        wg.setClientRequest(request);

        OperationHooks.OperationHookBody<I, O> body = new OperationHooks.OperationHookBody<>();
        body.setInput(input);
        body.set__wg(wg);
        if (ArrayUtil.isNotEmpty(originWg)) {
            wg.setUser(originWg[0].getUser());
            Optional.ofNullable(originWg[0].getClientRequest())
                    .map(WunderGraphRequest::getHeaders)
                    .ifPresent(request::setHeaders);
        }

        String respBody = HttpUtil.post(internalBaseURL + requestURI, JSON.toJSONString(body), timeout);
        OperationHooks.OperationBodyResponse<O> response = JSONObject.parseObject(respBody, new TypeReference<OperationHooks.OperationBodyResponse<O>>() {});
        return response.getData();
    }

    @AllArgsConstructor
    public static class Meta<I, O> {
        @Getter
        private final String path;

        public O execute(I input, BaseRequestBodyWg... originWg) {
            return InternalRequest.execute(this, input, originWg);
        }
    }
}
