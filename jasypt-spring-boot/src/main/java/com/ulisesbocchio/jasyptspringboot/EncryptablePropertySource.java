package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.exception.DecryptionException;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public interface EncryptablePropertySource<T> {
    public default Object getProperty(StringEncryptor encryptor, PropertySource<T> source, String name) {
        Object value = source.getProperty(name);
        if(value instanceof String) {
            String stringValue = String.valueOf(value);
            if(PropertyValueEncryptionUtils.isEncryptedValue(stringValue)) {
                try {
                    value = PropertyValueEncryptionUtils.decrypt(stringValue, encryptor);
                } catch (EncryptionOperationNotPossibleException e) {
                    throw new DecryptionException("Decryption of Properties failed,  make sure encryption/decryption " +
                            "passwords match", e);
                }
            }
        }
        return value;
    }
}
