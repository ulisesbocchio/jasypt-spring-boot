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
import org.springframework.core.env.StandardEnvironment;

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
public class StandardEncryptableEnvironment extends StandardEnvironment implements ConfigurableEnvironment {

    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final InterceptionMode interceptionMode;
    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public StandardEncryptableEnvironment() {
        this(InterceptionMode.WRAPPER);
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode) {
        this(interceptionMode, new DefaultPropertyDetector(), new DefaultPropertyFilter());
    }


    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor) {
        this(interceptionMode, new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector()), new DefaultPropertyFilter());
    }

    public StandardEncryptableEnvironment(StringEncryptor encryptor) {
        this(InterceptionMode.WRAPPER, new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector()), new DefaultPropertyFilter());
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        this(interceptionMode, new DefaultPropertyResolver(encryptor, detector), new DefaultPropertyFilter());
    }

    public StandardEncryptableEnvironment(EncryptablePropertyResolver resolver) {
        this(InterceptionMode.WRAPPER, resolver, new DefaultPropertyFilter());
    }

    public StandardEncryptableEnvironment(EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, filter);
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, EncryptablePropertyFilter filter) {
        this(interceptionMode, new DefaultPropertyDetector(), filter);
    }


    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyFilter filter) {
        this(interceptionMode, new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector()), filter);
    }

    public StandardEncryptableEnvironment(StringEncryptor encryptor, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector()), filter);
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this(interceptionMode, new DefaultPropertyResolver(encryptor, detector), filter);
    }

    public StandardEncryptableEnvironment(EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, resolver, filter);
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this.interceptionMode = interceptionMode;
        this.filter = filter;
        this.resolver = resolver;
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this.interceptionMode = interceptionMode;
        this.filter = filter;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), detector);
        actuallyCustomizePropertySources();
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        this.originalPropertySources = propertySources;
    }

    protected void actuallyCustomizePropertySources() {
        convertPropertySources(interceptionMode, resolver, filter, originalPropertySources);
        this.encryptablePropertySources = proxyPropertySources(interceptionMode, resolver, filter, originalPropertySources);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return this.encryptablePropertySources;
    }
}
