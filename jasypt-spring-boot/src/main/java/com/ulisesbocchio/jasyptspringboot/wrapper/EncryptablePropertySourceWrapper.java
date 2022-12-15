package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.PropertySource;

/**
 * <p>Wrapper for {@link org.springframework.core.env.PropertySource} instances that simply delegates the {@link #getProperty} method
 * to the {@link org.springframework.core.env.PropertySource} delegate instance to retrieve properties, while checking if the resulting
 * property is encrypted or not using the Jasypt convention of surrounding encrypted values with "ENC()".</p>
 * <p>When an encrypted property is detected, it is decrypted using the provided {@link org.jasypt.encryption.StringEncryptor}</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class EncryptablePropertySourceWrapper<T> extends PropertySource<T> implements EncryptablePropertySource<T> {
    private final CachingDelegateEncryptablePropertySource<T> encryptableDelegate;

    /**
     * <p>Constructor for EncryptablePropertySourceWrapper.</p>
     *
     * @param delegate a {@link org.springframework.core.env.PropertySource} object
     * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
     * @param filter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
     */
    public EncryptablePropertySourceWrapper(PropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        encryptableDelegate = new CachingDelegateEncryptablePropertySource<>(delegate, resolver, filter);
    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(String name) {
        return encryptableDelegate.getProperty(name);
    }

    /** {@inheritDoc} */
    @Override
    public PropertySource<T> getDelegate() {
        return encryptableDelegate;
    }
}
