package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.properties.PropertyFinder;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author Ulises Bocchio
 */
public class EncryptableMapPropertySourceWrapper extends MapPropertySource implements EncryptablePropertySource<Map<String, Object>> {
    private final StringEncryptor encryptor;
    private final MapPropertySource delegate;
    private final PropertyFinder propertyFinder;

    public EncryptableMapPropertySourceWrapper(MapPropertySource delegate, StringEncryptor encryptor, PropertyFinder propertyFinder) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(encryptor, "StringEncryptor cannot be null");
        Assert.notNull(propertyFinder, "PropertyFinder cannot be null");
        this.encryptor = encryptor;
        this.delegate = delegate;
        this.propertyFinder = propertyFinder;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(encryptor, delegate, name);
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
