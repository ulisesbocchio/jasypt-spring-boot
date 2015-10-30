package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public interface EncryptablePropertySource<T> {
    static class DefaultMethods<T> {
      public Object getProperty(StringEncryptor encryptor, PropertySource<T> source, String name) {
        Object value = source.getProperty(name);
        if(value instanceof String) {
          String stringValue = String.valueOf(value);
          if(PropertyValueEncryptionUtils.isEncryptedValue(stringValue)) {
            value = PropertyValueEncryptionUtils.decrypt(stringValue, encryptor);
          }
        }
        return value;
      }
    }
}
