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
import org.springframework.core.env.StandardEnvironment;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.convertPropertySources;
import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.proxyPropertySources;

public class StandardEncryptableEnvironment extends StandardEnvironment implements ConfigurableEnvironment {

    private final EncryptablePropertyResolver resolver;
    private final InterceptionMode interceptionMode;
    private MutablePropertySources encryptablePropertySources;
    private MutablePropertySources originalPropertySources;

    public StandardEncryptableEnvironment() {
        this(InterceptionMode.WRAPPER);
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), new DefaultPropertyDetector());
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, EncryptablePropertyDetector detector) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(new DefaultLazyEncryptor(this), detector);
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector());
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableEnvironment(StringEncryptor encryptor) {
        this.interceptionMode = InterceptionMode.WRAPPER;
        this.resolver = new DefaultPropertyResolver(encryptor, new DefaultPropertyDetector());
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        this.interceptionMode = interceptionMode;
        this.resolver = new DefaultPropertyResolver(encryptor, detector);
        actuallyCustomizePropertySources();
    }

    public StandardEncryptableEnvironment(InterceptionMode interceptionMode, EncryptablePropertyResolver resolver) {
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
