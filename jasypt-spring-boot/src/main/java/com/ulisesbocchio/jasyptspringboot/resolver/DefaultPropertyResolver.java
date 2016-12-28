package com.ulisesbocchio.jasyptspringboot.resolver;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.exception.DecryptionException;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.util.Assert;

/**
 * @author Ulises Bocchio
 */
public class DefaultPropertyResolver implements EncryptablePropertyResolver {

    private StringEncryptor encryptor;
    private EncryptablePropertyDetector detector;

    public DefaultPropertyResolver(StringEncryptor encryptor) {
        this(encryptor, new DefaultPropertyDetector());
    }

    public DefaultPropertyResolver(StringEncryptor encryptor, EncryptablePropertyDetector detector) {
        Assert.notNull(encryptor, "String encryptor can't be null");
        Assert.notNull(detector, "Encryptable Property detector can't be null");
        this.encryptor = encryptor;
        this.detector = detector;
    }

    @Override
    public String resolvePropertyValue(String value) {
        String actualValue = value;
        if (detector.isEncrypted(value)) {
            try {
                actualValue = encryptor.decrypt(detector.unwrapEncryptedValue(value.trim()));
            } catch (EncryptionOperationNotPossibleException e) {
                throw new DecryptionException("Decryption of Properties failed,  make sure encryption/decryption " +
                        "passwords match", e);
            }
        }
        return actualValue;
    }
}
