package com.geovannycode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Optional;

@SpringBootApplication
public class McpClientExampleApplication {

    private static final Logger logger = LoggerFactory.getLogger(McpClientExampleApplication.class);

    private static final String SEPARATOR = "=".repeat(60);


    public static void main(String[] args) {
        var context = SpringApplication.run(McpClientExampleApplication.class, args);
        logApplicationInfo(context);
    }

    private static void logApplicationInfo(ConfigurableApplicationContext context) {
        var environment = context.getEnvironment();

        logger.info(SEPARATOR);
        logger.info("  MCP Client Application iniciada exitosamente");
        logger.info("  Versión: {}", getApplicationVersion());
        logger.info("  Puerto: {}", getServerPort(environment));
        logger.info("  Perfil activo: {}", getActiveProfiles(environment));
        logger.info("  URL: http://localhost:{}", getServerPort(environment));
        logger.info(SEPARATOR);

        logEnabledFeatures(environment);
    }


    private static String getApplicationVersion() {
        var version = McpClientExampleApplication.class.getPackage().getImplementationVersion();
        return Optional.ofNullable(version).orElse("1.0.0");
    }


    private static String getServerPort(Environment environment) {
        return environment.getProperty("server.port", "8080");
    }


    private static String getActiveProfiles(Environment environment) {
        var profiles = environment.getActiveProfiles();
        return profiles.length > 0
                ? String.join(", ", profiles)
                : "default";
    }


    private static void logEnabledFeatures(Environment environment) {
        logger.info("Características habilitadas:");

        checkFeature(environment, "spring.ai.openai.api-key", "- OpenAI Integration");
        checkFeature(environment, "spring.ai.mcp.client.name", "- MCP Client");
        checkFeature(environment, "management.endpoints.web.exposure.include", "- Actuator Endpoints");
        checkFeature(environment, "spring.security.user.name", "- Spring Security");
    }


    private static void checkFeature(Environment environment, String property, String featureName) {
        Optional.ofNullable(environment.getProperty(property))
                .filter(value -> !value.isBlank())
                .ifPresent(value -> logger.info(featureName + " ✓"));
    }
}
