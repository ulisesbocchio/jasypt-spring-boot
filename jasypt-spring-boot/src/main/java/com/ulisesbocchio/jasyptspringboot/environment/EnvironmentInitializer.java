package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.*;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultLazyPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableMutablePropertySourcesWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>EnvironmentInitializer class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
@Slf4j
public class EnvironmentInitializer {
    public static final String JASYPT_INITIALIZER_INSTANCE = "jasypt.initializer.instance";
    public static final String JASYPT_INITIALIZER_SOURCE_NAME = "jasyptInitializer";
    private final InterceptionMode interceptionMode;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final StringEncryptor encryptor;
    private final EncryptablePropertyDetector detector;
    private final InterceptionMode propertySourceInterceptionMode;

    /**
     * <p>Constructor for EnvironmentInitializer.</p>
     *
     * @param interceptionMode a {@link com.ulisesbocchio.jasyptspringboot.InterceptionMode} object
     * @param propertySourceInterceptionMode a {@link com.ulisesbocchio.jasyptspringboot.InterceptionMode} object
     * @param skipPropertySourceClasses a {@link java.util.List} object
     * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
     * @param filter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
     * @param encryptor a {@link org.jasypt.encryption.StringEncryptor} object
     * @param detector a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector} object
     */
    public EnvironmentInitializer(InterceptionMode interceptionMode, InterceptionMode propertySourceInterceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, StringEncryptor encryptor, EncryptablePropertyDetector detector) {

        this.interceptionMode = interceptionMode;
        this.propertySourceInterceptionMode = propertySourceInterceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.resolver = resolver;
        this.filter = filter;
        this.encryptor = encryptor;
        this.detector = detector;
    }

    public void initializeBootstrap(ConfigurableEnvironment environment) {
        log.info("Initializing Bootstrap Environment: {}", environment.getClass().getSimpleName());
        EnvCopy envCopy = new EnvCopy(environment);
        EncryptablePropertySourceConverter converter = createConverter(environment, envCopy);
        converter.convertPropertySources(environment.getPropertySources());
    }

    public void initialize(EncryptableEnvironment environment) {
        log.info("Initializing Environment: {}", environment.getClass().getSimpleName());
        EnvCopy envCopy = new EnvCopy(environment);
        EncryptablePropertySourceConverter converter = createConverter(environment, envCopy);
        converter.convertPropertySources(environment.getOriginalPropertySources());
        MutablePropertySources encryptableSources = converter.convertMutablePropertySources(propertySourceInterceptionMode, environment.getOriginalPropertySources(), envCopy);
        // We inject a special property source with this initializer in the custom environment.
        // This allows BootstrapSpringApplicationListener to detect the custom environment on a bootstrap (cloud) environment
        // and initialize it also, so all bootstrap property sources can be encryptable.
        // Also, EncryptableLoggingEnvironmentListener uses this hook to detect the custom environment and
        // re-initializes the logging environment which would have been populated with encrypted values
        // that should have been decrypted.
        MapPropertySource initializerSource = new MapPropertySource(JASYPT_INITIALIZER_SOURCE_NAME,
                Map.of(JASYPT_INITIALIZER_INSTANCE, this));
        if (encryptableSources instanceof EncryptableMutablePropertySourcesWrapper) {
            ((EncryptableMutablePropertySourcesWrapper) encryptableSources).addLastClean(initializerSource);
        } else {
            encryptableSources.addLast(initializerSource);
        }
        environment.setEncryptablePropertySources(encryptableSources);
    }

    private EncryptablePropertySourceConverter createConverter(ConfigurableEnvironment environment, EnvCopy envCopy) {
        InterceptionMode actualInterceptionMode = Optional.ofNullable(interceptionMode).orElse(InterceptionMode.WRAPPER);
        List<Class<PropertySource<?>>> actualSkipPropertySourceClasses = Optional.ofNullable(skipPropertySourceClasses).orElseGet(Collections::emptyList);
        EncryptablePropertyFilter actualFilter = Optional.ofNullable(filter).orElseGet(() -> new DefaultLazyPropertyFilter(envCopy.get()));
        StringEncryptor actualEncryptor = Optional.ofNullable(encryptor).orElseGet(() -> new DefaultLazyEncryptor(envCopy.get()));
        EncryptablePropertyDetector actualDetector = Optional.ofNullable(detector).orElseGet(() -> new DefaultLazyPropertyDetector(envCopy.get()));
        EncryptablePropertyResolver actualResolver = Optional.ofNullable(resolver).orElseGet(() -> new DefaultLazyPropertyResolver(actualDetector, actualEncryptor, environment));
        return new EncryptablePropertySourceConverter(actualInterceptionMode, actualSkipPropertySourceClasses, actualResolver, actualFilter);
    }

    static MutableConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources) {
        return new MutableConfigurablePropertyResolver(propertySources, ConfigurationPropertySources::createPropertyResolver);
    }

}
