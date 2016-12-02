package com.ulisesbocchio.jasyptspringboot.properties;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;

public class JasyptPropertyFinder implements PropertyFinder {
    @Override
    public boolean isEncryptedValue(String stringValue) {
        return PropertyValueEncryptionUtils.isEncryptedValue(stringValue);
    }

    @Override
    public String decrypt(final String encodedValue, final StringEncryptor encryptor) {
        return PropertyValueEncryptionUtils.decrypt(encodedValue, encryptor);
    }
}
