package io.fireboom.plugins;


import cn.hutool.core.util.ArrayUtil;
import io.fireboom.server.authentication.MutatingPostAuthenticationResponse;
import io.fireboom.server.enums.MiddlewareHook;
import io.fireboom.server.hook.BaseRequestBodyWg;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class AuthenticationHooks {

    public static Consumer<BaseRequestBodyWg> postAuthentication = body -> {
        throw new RuntimeException("404 not found");
    };
    public static Function<BaseRequestBodyWg, MutatingPostAuthenticationResponse> mutatingPostAuthentication = body -> {
        throw new RuntimeException("404 not found");
    };
    public static Function<BaseRequestBodyWg, MutatingPostAuthenticationResponse> revalidateAuthentication = body -> {
        throw new RuntimeException("404 not found");
    };
    public static Consumer<BaseRequestBodyWg> postLogout = body -> {
        throw new RuntimeException("404 not found");
    };

    @PostConstruct
    protected void init() {
        Class<?>[] interfaces = this.getClass().getInterfaces();
        if (ArrayUtil.contains(interfaces, PostAuthentication.class)) {
            postAuthentication = ((PostAuthentication) this)::execute;
            log.info("register authentication hook [{}]", MiddlewareHook.postAuthentication);
            return;
        }

        if (ArrayUtil.contains(interfaces, MutatingPostAuthentication.class)) {
            mutatingPostAuthentication = ((MutatingPostAuthentication) this)::execute;
            log.info("register authentication hook [{}]", MiddlewareHook.mutatingPostAuthentication);
            return;
        }

        if (ArrayUtil.contains(interfaces, RevalidateAuthentication.class)) {
            revalidateAuthentication = ((RevalidateAuthentication) this)::execute;
            log.info("register authentication hook [{}]", MiddlewareHook.revalidateAuthentication);
            return;
        }

        if (ArrayUtil.contains(interfaces, PostLogout.class)) {
            postLogout = ((PostLogout) this)::execute;
            log.info("register authentication hook [{}]", MiddlewareHook.postLogout);
        }
    }

    public interface PostAuthentication {
        void execute(BaseRequestBodyWg body);
    }

    public interface MutatingPostAuthentication {
        MutatingPostAuthenticationResponse execute(BaseRequestBodyWg body);
    }

    public interface RevalidateAuthentication {
        MutatingPostAuthenticationResponse execute(BaseRequestBodyWg body);
    }

    public interface PostLogout {
        void execute(BaseRequestBodyWg body);
    }
}
