package io.fireboom.plugins;

import io.fireboom.server.enums.Endpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Slf4j
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private long startTime;
    AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startTime = System.currentTimeMillis();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestURI = request.getRequestURI();
        if (Arrays.stream(Endpoint.values()).noneMatch(x -> pathMatcher.match(x.getValue(), requestURI))) {
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("request url: {}, cost: {}ms, status: {}, exception: {}", requestURI, duration, response.getStatus(), ex);
    }
}
