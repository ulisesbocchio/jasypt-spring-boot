package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultLazyPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import lombok.Builder;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.convertPropertySources;
import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.proxyPropertySources;

/**
 * A custom {@link ConfigurableEnvironment} that is useful for
 * early access of encrypted properties on bootstrap. While not required in most scenarios
 * could be useful when customizing Spring Boot's init behavior or integrating with certain capabilities that are
 * configured very early, such as Logging configuration. For a concrete example, this method of enabling encryptable
 * properties is the only one that works with Spring Properties replacement in logback-spring.xml files, using the
 * springProperty tag
 */
public class StandardEncryptableServletEnvironment extends StandardServletEnvironment implements ConfigurableEnvironment {

    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public StandardEncryptableServletEnvironment() {
        this(null, null, null, null, null, null);
    }

    /**
     * Create a new Encryptable Environment. All arguments are optional, provide null if default value is desired.
     *
     * @param interceptionMode          The interception method to utilize, or null (Default is {@link InterceptionMode#WRAPPER})
     * @param skipPropertySourceClasses A list of {@link PropertySource} classes to skip from interception, or null (Default is empty)
     * @param resolver                  The property resolver to utilize, or null (Default is {@link DefaultLazyPropertyResolver}  which will resolve to specified configuration)
     * @param filter                    The property filter to utilize, or null (Default is {@link DefaultLazyPropertyFilter}  which will resolve to specified configuration)
     * @param encryptor                 The string encryptor to utilize, or null (Default is {@link DefaultLazyEncryptor} which will resolve to specified configuration)
     * @param detector                  The property detector to utilize, or null (Default is {@link DefaultLazyPropertyDetector} which will resolve to specified configuration)
     */
    @Builder
    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        InterceptionMode actualInterceptionMode = Optional.ofNullable(interceptionMode).orElse(InterceptionMode.WRAPPER);
        List<Class<PropertySource<?>>> actualSkipPropertySourceClasses = Optional.ofNullable(skipPropertySourceClasses).orElseGet(Collections::emptyList);
        EnvCopy envCopy = new EnvCopy(this);
        EncryptablePropertyFilter actualFilter = Optional.ofNullable(filter).orElseGet(() -> new DefaultLazyPropertyFilter(envCopy.get()));
        StringEncryptor actualEncryptor = Optional.ofNullable(encryptor).orElseGet(() -> new DefaultLazyEncryptor(envCopy.get()));
        EncryptablePropertyDetector actualDetector = Optional.ofNullable(detector).orElseGet(() -> new DefaultLazyPropertyDetector(envCopy.get()));
        EncryptablePropertyResolver actualResolver = Optional.ofNullable(resolver).orElseGet(() -> new DefaultLazyPropertyResolver(actualDetector, actualEncryptor, this));
        convertPropertySources(actualInterceptionMode, actualSkipPropertySourceClasses, actualResolver, actualFilter, originalPropertySources);
        this.encryptablePropertySources = proxyPropertySources(actualInterceptionMode, actualSkipPropertySourceClasses, actualResolver, actualFilter, originalPropertySources, envCopy);
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        this.originalPropertySources = propertySources;
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return this.encryptablePropertySources;
    }
}
