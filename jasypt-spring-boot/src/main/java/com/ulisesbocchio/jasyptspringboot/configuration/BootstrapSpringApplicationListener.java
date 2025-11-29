package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.environment.EnvironmentInitializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import static com.ulisesbocchio.jasyptspringboot.environment.EnvironmentInitializer.JASYPT_INITIALIZER_INSTANCE;
import static com.ulisesbocchio.jasyptspringboot.environment.EnvironmentInitializer.JASYPT_INITIALIZER_SOURCE_NAME;

/**
 * We inject a special property source within EnvironmentInitializer in the custom environment.
 * This allows BootstrapSpringApplicationListener to detect the custom environment on a bootstrap (cloud) environment
 * and initialize it also, so all bootstrap property sources can be encryptable.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class BootstrapSpringApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    public static final String BOOTSTRAP_PROPERTY_SOURCE_NAME = "bootstrap";
    @Getter
    private static SpringApplication springApplication;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // Wrap property sources here
        log.info("Found Environment: {}", environment.getClass().getName());
        springApplication = event.getSpringApplication();

        // listen to events in a bootstrap context
        if (environment.getPropertySources().contains(BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            log.info("Bootstrap Environment detected!");
            PropertySource<?> jps = environment.getPropertySources().remove(JASYPT_INITIALIZER_SOURCE_NAME);
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
