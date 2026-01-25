package com.ulisesbocchio.jasyptspringboot.configuration;

import ch.qos.logback.classic.LoggerContext;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

/**
 * <p>{@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that wraps all {@link org.springframework.core.env.PropertySource} defined in the {@link org.springframework.core.env.Environment}
 * with {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper} and defines a default {@link
 * EncryptablePropertyResolver} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>
 * <p>It takes the lowest precedence so it does not interfere with Spring Boot's own post processors</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
@Slf4j
public class EnableEncryptablePropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware, Ordered {

    private final ConfigurableEnvironment environment;
    private final EncryptablePropertySourceConverter converter;
    private ApplicationContext applicationContext;

    /**
     * <p>Constructor for EnableEncryptablePropertiesBeanFactoryPostProcessor.</p>
     *
     * @param environment a {@link org.springframework.core.env.ConfigurableEnvironment} object
     * @param converter   a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter} object
     */
    public EnableEncryptablePropertiesBeanFactoryPostProcessor(ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        this.environment = environment;
        this.converter = converter;
    }

    private void reinitializeLoggingSystem() {
        log.info("Re-initializing Logging System");
        // Reinitialize logging system to pick up any encrypted properties used in logging configuration
        LoggingApplicationListener loggingListener = ((AbstractApplicationContext) this.applicationContext)
                .getApplicationListeners()
                .stream()
                .filter(LoggingApplicationListener.class::isInstance)
                .map(LoggingApplicationListener.class::cast)
                .findFirst()
                .orElse(null);

        ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent = BootstrapSpringApplicationListener.getApplicationEnvironmentPreparedEvent();
        if (loggingListener != null && applicationEnvironmentPreparedEvent != null) {
            LoggingSystem loggingSystem = LoggingSystem.get(applicationEnvironmentPreparedEvent.getSpringApplication().getClassLoader());
            // Reset logging system
            loggingSystem.cleanUp();
            for (LoggingSystemProperty property : LoggingSystemProperty.values()) {
                System.clearProperty(property.getEnvironmentVariableName());
            }
            loggingListener.onApplicationEvent(applicationEnvironmentPreparedEvent);
            log.info("Logging System re-initialized by LoggingApplicationListener");
        }
    }

    /**
     * Make sure the system environment wrapper is initialized with setWrapGetSource=true
     * For custom environments this is done early on in {@link EncryptableSystemEnvironmentPropertySourceWrapperGetSourceWrapperEnvironmentListener}
     * We'll force the call to that logic here via the static method.
     */
    private void enableSystemEnvironmentSourceEncryptableMapWrapper() {
        log.info("Re-initializing EncryptableSystemEnvironmentPropertySourceWrapper map delegation");
        EncryptableSystemEnvironmentPropertySourceWrapperGetSourceWrapperEnvironmentListener.enableGetSourceWrapping(environment);
        log.info("EncryptableSystemEnvironmentPropertySourceWrapper map delegation re-initialized");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("Post-processing PropertySource instances");
        MutablePropertySources propSources = environment.getPropertySources();
        converter.convertPropertySources(propSources);
        this.enableSystemEnvironmentSourceEncryptableMapWrapper();
        this.reinitializeLoggingSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
