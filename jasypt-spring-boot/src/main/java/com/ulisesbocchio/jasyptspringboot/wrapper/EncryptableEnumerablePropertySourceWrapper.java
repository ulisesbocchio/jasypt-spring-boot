package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.properties.PropertyFinder;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.Assert;

/**
 * @author Ulises Bocchio
 */
public class EncryptableEnumerablePropertySourceWrapper<T> extends EnumerablePropertySource<T> implements EncryptablePropertySource<T> {
    private final EnumerablePropertySource<T> delegate;
    private final StringEncryptor encryptor;
    private final PropertyFinder propertyFinder;

    public EncryptableEnumerablePropertySourceWrapper(EnumerablePropertySource<T> delegate, StringEncryptor encryptor, PropertyFinder propertyFinder) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(encryptor, "StringEncryptor cannot be null");
        Assert.notNull(propertyFinder, "PropertyFinder cannot be null");
        this.delegate = delegate;
        this.encryptor = encryptor;
        this.propertyFinder = propertyFinder;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(encryptor, delegate, name);
    }

    @Override
    public String[] getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public boolean isEncryptedValue(String stringValue) {
        return propertyFinder.isEncryptedValue(stringValue);
    }

    @Override
    public String decrypt(String encodedValue, StringEncryptor encryptor) {
        return propertyFinder.decrypt(encodedValue, encryptor);
    }
}
