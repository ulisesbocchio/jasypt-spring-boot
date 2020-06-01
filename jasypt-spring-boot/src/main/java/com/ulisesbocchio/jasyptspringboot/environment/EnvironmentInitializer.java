package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.*;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultLazyPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EnvironmentInitializer {
    private final ConfigurableEnvironment environment;
    private final InterceptionMode interceptionMode;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final StringEncryptor encryptor;
    private final EncryptablePropertyDetector detector;

    public EnvironmentInitializer(ConfigurableEnvironment environment, InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, StringEncryptor encryptor, EncryptablePropertyDetector detector) {

        this.environment = environment;
        this.interceptionMode = interceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.resolver = resolver;
        this.filter = filter;
        this.encryptor = encryptor;
        this.detector = detector;
    }

    MutablePropertySources initialize(MutablePropertySources originalPropertySources) {
        InterceptionMode actualInterceptionMode = Optional.ofNullable(interceptionMode).orElse(InterceptionMode.WRAPPER);
        List<Class<PropertySource<?>>> actualSkipPropertySourceClasses = Optional.ofNullable(skipPropertySourceClasses).orElseGet(Collections::emptyList);
        EnvCopy envCopy = new EnvCopy(environment);
        EncryptablePropertyFilter actualFilter = Optional.ofNullable(filter).orElseGet(() -> new DefaultLazyPropertyFilter(envCopy.get()));
        StringEncryptor actualEncryptor = Optional.ofNullable(encryptor).orElseGet(() -> new DefaultLazyEncryptor(envCopy.get()));
        EncryptablePropertyDetector actualDetector = Optional.ofNullable(detector).orElseGet(() -> new DefaultLazyPropertyDetector(envCopy.get()));
        EncryptablePropertyResolver actualResolver = Optional.ofNullable(resolver).orElseGet(() -> new DefaultLazyPropertyResolver(actualDetector, actualEncryptor, environment));
        EncryptablePropertySourceConverter converter = new EncryptablePropertySourceConverter(actualInterceptionMode, actualSkipPropertySourceClasses, actualResolver, actualFilter);
        converter.convertPropertySources(originalPropertySources);
        return converter.proxyPropertySources(originalPropertySources, envCopy);
    }
}