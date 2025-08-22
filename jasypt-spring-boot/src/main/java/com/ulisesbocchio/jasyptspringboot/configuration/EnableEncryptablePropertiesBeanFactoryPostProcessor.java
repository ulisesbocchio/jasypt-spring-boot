package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.boot.system.ApplicationPid;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Collections;
import java.util.Map;

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
     * @param converter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter} object
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
        
        SpringApplication springApplication = BootstrapSpringApplicationListener.getSpringApplication();
        if (loggingListener != null && springApplication != null) {
            // Reset logging system
            for (LoggingSystemProperty property : LoggingSystemProperty.values()) {
                System.clearProperty(property.getEnvironmentVariableName());
            }
            loggingListener.onApplicationEvent(new ApplicationEnvironmentPreparedEvent(null, springApplication, null, environment));
            log.info("Logging System re-initialized by LoggingApplicationListener");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("Post-processing PropertySource instances");
        MutablePropertySources propSources = environment.getPropertySources();
        converter.convertPropertySources(propSources);
        this.reinitializeLoggingSystem();
    }

    /** {@inheritDoc} */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
