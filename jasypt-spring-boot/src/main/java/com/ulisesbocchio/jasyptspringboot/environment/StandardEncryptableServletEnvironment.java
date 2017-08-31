package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultPropertyResolver;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.context.support.StandardServletEnvironment;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.convertPropertySources;
import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.proxyPropertySources;

public class StandardEncryptableServletEnvironment extends StandardServletEnvironment implements ConfigurableEnvironment {

    private final EncryptablePropertyResolver resolver;
    private final InterceptionMode interceptionMode;
    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public StandardEncryptableServletEnvironment() {
        this(InterceptionMode.WRAPPER);
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), new DefaultPropertyDetector());
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, EncryptablePropertyDetector detector) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), detector);
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector());
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(encryptor, detector);
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableServletEnvironment(InterceptionMode interceptionMode, EncryptablePropertyResolver resolver) {
        this.interceptionMode = interceptionMode;
        this.resolver = resolver;
        actuallyCustomizePropertySources();
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        super.customizePropertySources(propertySources);
        this.originalPropertySources = propertySources;
    }

    protected void actuallyCustomizePropertySources() {
        convertPropertySources(interceptionMode, resolver, originalPropertySources);
        encryptablePropertySources = proxyPropertySources(interceptionMode, resolver, originalPropertySources);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return encryptablePropertySources;
    }
}
