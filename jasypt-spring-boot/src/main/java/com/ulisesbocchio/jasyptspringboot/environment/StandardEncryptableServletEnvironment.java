package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultPropertyResolver;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.util.Collections;
import java.util.List;

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

    private EncryptablePropertyResolver resolver;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;
    private final EncryptablePropertyFilter filter;
    private final InterceptionMode interceptionMode;
    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public StandardEncryptableServletEnvironment() {
        this(InterceptionMode.WRAPPER);
    }

    public StandardEncryptableServletEnvironment(List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        this(InterceptionMode.WRAPPER, skipPropertySourceClasses);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode) {
        this(interceptionMode, Collections.emptyList(), new DefaultPropertyDetector(), new DefaultPropertyFilter());
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        this(interceptionMode, skipPropertySourceClasses, new DefaultPropertyDetector(), new DefaultPropertyFilter());
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor) {
        this(interceptionMode, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, StringEncryptor encryptor) {
        this(interceptionMode, skipPropertySourceClasses, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(StringEncryptor encryptor) {
        this(InterceptionMode.WRAPPER, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(StringEncryptor encryptor, List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        this(InterceptionMode.WRAPPER, skipPropertySourceClasses, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        this(interceptionMode, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, detector, this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        this(interceptionMode, skipPropertySourceClasses, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, detector, this);
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyResolver resolver) {
        this(InterceptionMode.WRAPPER, Collections.emptyList(), resolver, new DefaultPropertyFilter());
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyResolver resolver, List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        this(InterceptionMode.WRAPPER, skipPropertySourceClasses, resolver, new DefaultPropertyFilter());
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, filter);
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyFilter filter, List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        this(InterceptionMode.WRAPPER, skipPropertySourceClasses, filter);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, EncryptablePropertyFilter filter) {
        this(interceptionMode, Collections.emptyList(), new DefaultPropertyDetector(), filter);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyFilter filter) {
        this(interceptionMode, skipPropertySourceClasses, new DefaultPropertyDetector(), filter);
    }


    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyFilter filter) {
        this(interceptionMode, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(StringEncryptor encryptor, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, Collections.emptyList(), resolver, filter);
    }

    public StandardEncryptableServletEnvironment(List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, skipPropertySourceClasses, resolver, filter);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this(interceptionMode, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, detector, this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, StringEncryptor encryptor, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this(interceptionMode, skipPropertySourceClasses, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, detector, this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this.interceptionMode = interceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.filter = filter;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), detector, this);
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this.interceptionMode = interceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.filter = filter;
        this.resolver = resolver;
        actuallyCustomizePropertySources();
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        this.originalPropertySources = propertySources;
    }

    protected void actuallyCustomizePropertySources() {
        convertPropertySources(interceptionMode, skipPropertySourceClasses, resolver, filter, originalPropertySources);
        this.encryptablePropertySources = proxyPropertySources(interceptionMode, skipPropertySourceClasses, resolver, filter, originalPropertySources);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return this.encryptablePropertySources;
    }
}
