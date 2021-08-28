package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.*;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultLazyPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class EnvironmentInitializer {
    private final InterceptionMode interceptionMode;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final StringEncryptor encryptor;
    private final EncryptablePropertyDetector detector;
    private final InterceptionMode propertySourceInterceptionMode;

    public EnvironmentInitializer(InterceptionMode interceptionMode, InterceptionMode propertySourceInterceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, StringEncryptor encryptor, EncryptablePropertyDetector detector) {

        this.interceptionMode = interceptionMode;
        this.propertySourceInterceptionMode = propertySourceInterceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.resolver = resolver;
        this.filter = filter;
        this.encryptor = encryptor;
        this.detector = detector;
    }

    void initialize(EncryptableEnvironment environment) {
        log.info("Initializing Environment: {}", environment.getClass().getSimpleName());
        InterceptionMode actualInterceptionMode = Optional.ofNullable(interceptionMode).orElse(InterceptionMode.WRAPPER);
        List<Class<PropertySource<?>>> actualSkipPropertySourceClasses = Optional.ofNullable(skipPropertySourceClasses).orElseGet(Collections::emptyList);
        EnvCopy envCopy = new EnvCopy(environment);
        EncryptablePropertyFilter actualFilter = Optional.ofNullable(filter).orElseGet(() -> new DefaultLazyPropertyFilter(envCopy.get()));
        StringEncryptor actualEncryptor = Optional.ofNullable(encryptor).orElseGet(() -> new DefaultLazyEncryptor(envCopy.get()));
        EncryptablePropertyDetector actualDetector = Optional.ofNullable(detector).orElseGet(() -> new DefaultLazyPropertyDetector(envCopy.get()));
        EncryptablePropertyResolver actualResolver = Optional.ofNullable(resolver).orElseGet(() -> new DefaultLazyPropertyResolver(actualDetector, actualEncryptor, environment));
        EncryptablePropertySourceConverter converter = new EncryptablePropertySourceConverter(actualInterceptionMode, actualSkipPropertySourceClasses, actualResolver, actualFilter);
        converter.convertPropertySources(environment.getOriginalPropertySources());
        MutablePropertySources encryptableSources = converter.convertMutablePropertySources(propertySourceInterceptionMode, environment.getOriginalPropertySources(), envCopy);
        environment.setEncryptablePropertySources(encryptableSources);
    }

    static MutableConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources) {
        return new MutableConfigurablePropertyResolver(propertySources, ConfigurationPropertySources::createPropertyResolver);
    }

}