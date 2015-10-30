package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.Assert;

/**
 * @author Ulises Bocchio
 */
public class EncryptableEnumerablePropertySourceWrapper<T> extends EnumerablePropertySource<T> implements EncryptablePropertySource<T> {
    private final EnumerablePropertySource<T> delegate;
    private final StringEncryptor encryptor;

    public EncryptableEnumerablePropertySourceWrapper(EnumerablePropertySource<T> delegate, StringEncryptor encryptor) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(encryptor, "StringEncryptor cannot be null");
        this.delegate = delegate;
        this.encryptor = encryptor;
    }

    @Override
    public Object getProperty(String name) {
        return new DefaultMethods<T>().getProperty(encryptor, delegate, name);
    }

    @Override
    public String[] getPropertyNames() {
        return delegate.getPropertyNames();
    }
}
