package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.environment.EnvironmentInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import static com.ulisesbocchio.jasyptspringboot.environment.EnvironmentInitializer.JASYPT_INITIALIZER_INSTANCE;
import static com.ulisesbocchio.jasyptspringboot.environment.EnvironmentInitializer.JASYPT_INITIALIZER_SOURCE_NAME;

/**
 * Needs to run after {@link org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor} and {@link org.springframework.cloud.bootstrap.BootstrapConfigFileApplicationListener}
 * This listener detects the custom environment created by {@link EnvironmentInitializer} and re-initializes the logging environment property sources
 * which would have been populated without the encryptable wrapper.
 * The {@link EnableEncryptablePropertiesBeanFactoryPostProcessor} then effectively re-initializes the logging {@link org.springframework.boot.logging.LoggingSystem}
 * @see EnvironmentInitializer
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 13)
@Slf4j
public class EncryptableLoggingEnvironmentListener implements EnvironmentPostProcessor {
    public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Wrap property sources here
        log.info("Found Environment: {}", environment.getClass().getName());

        // listen to events in a bootstrap context
        if (environment.getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            log.info("Bootstrap Environment detected!");
            PropertySource<?> jps = environment.getPropertySources().get(JASYPT_INITIALIZER_SOURCE_NAME);
            if (jps instanceof MapPropertySource) {
                EnvironmentInitializer ei = (EnvironmentInitializer) jps.getProperty(JASYPT_INITIALIZER_INSTANCE);
                if (ei != null) {
                    log.info("Found Environment Initializer!");
                    ei.initializeBootstrap(environment);
                }
            }
        }
    }
}
