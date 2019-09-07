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
import org.springframework.web.context.support.StandardServletEnvironment;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.convertPropertySources;
import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.proxyPropertySources;

public class StandardEncryptableServletEnvironment extends StandardServletEnvironment implements ConfigurableEnvironment {

    private EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final InterceptionMode interceptionMode;
    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public StandardEncryptableServletEnvironment() {
        this(InterceptionMode.WRAPPER);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode) {
        this(interceptionMode, new DefaultPropertyDetector(), new DefaultPropertyFilter());
    }


    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor) {
        this(interceptionMode, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(StringEncryptor encryptor) {
        this(InterceptionMode.WRAPPER, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        this(interceptionMode, new DefaultPropertyFilter());
        this.resolver = new DefaultPropertyResolver(encryptor, detector, this);
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyResolver resolver) {
        this(InterceptionMode.WRAPPER, resolver, new DefaultPropertyFilter());
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, filter);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, EncryptablePropertyFilter filter) {
        this(interceptionMode, new DefaultPropertyDetector(), filter);
    }


    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyFilter filter) {
        this(interceptionMode, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(StringEncryptor encryptor, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector(), this);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this(interceptionMode, filter);
        this.resolver = new DefaultPropertyResolver(encryptor, detector, this);
    }

    public StandardEncryptableServletEnvironment(EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this(InterceptionMode.WRAPPER, resolver, filter);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        this.interceptionMode = interceptionMode;
        this.filter = filter;
        this.resolver = resolver;
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, EncryptablePropertyDetector detector, EncryptablePropertyFilter filter) {
        this.interceptionMode = interceptionMode;
        this.filter = filter;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), detector, this);
        actuallyCustomizePropertySources();
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        this.originalPropertySources = propertySources;
    }

    protected void actuallyCustomizePropertySources() {
        convertPropertySources(interceptionMode, resolver, filter, originalPropertySources);
        encryptablePropertySources = proxyPropertySources(interceptionMode, resolver, filter, originalPropertySources);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return encryptablePropertySources;
    }
}
