package io.fireboom;

import io.fireboom.plugins.CustomizeHooks;
import io.fireboom.plugins.InternalRequest;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
@SpringBootApplication
public class SystemApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SystemApplication.class);
    }

    public static void main(String[] args) {
        ConfigurableEnvironment environment = SpringApplication.run(SystemApplication.class, args).getEnvironment();
        InternalRequest.internalBaseURL = environment.getProperty("fireboom.internalBaseURL");
        String nodeEnvFilepath = environment.getProperty("fireboom.nodeEnvFilepath");
        Dotenv.configure().filename(nodeEnvFilepath).systemProperties().load();
        CustomizeHooks.writeIntrospectJson();
    }
}
