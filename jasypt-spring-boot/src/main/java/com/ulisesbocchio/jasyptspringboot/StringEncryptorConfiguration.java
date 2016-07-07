package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class StringEncryptorConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(StringEncryptorConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(StringEncryptor.class)
    public StringEncryptor stringEncryptor(final Environment environment) {
        return new LazyStringEncryptor(new Supplier<StringEncryptor>() {
          @Override
          public StringEncryptor get() {
            PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPassword(getRequiredProperty(environment, "jasypt.encryptor.password"));
            config.setAlgorithm(getProperty(environment, "jasypt.encryptor.algorithm", "PBEWithMD5AndDES"));
            config.setKeyObtentionIterations(getProperty(environment, "jasypt.encryptor.keyObtentionIterations", "1000"));
            config.setPoolSize(getProperty(environment, "jasypt.encryptor.poolSize", "1"));
            config.setProviderName(getProperty(environment, "jasypt.encryptor.providerName", "SunJCE"));
            config.setSaltGeneratorClassName(getProperty(environment, "jasypt.encryptor.saltGeneratorClassname", "org.jasypt.salt.RandomSaltGenerator"));
            config.setStringOutputType(getProperty(environment, "jasypt.encryptor.stringOutputType", "base64"));
            encryptor.setConfig(config);
            return encryptor;
          }
        });
    }

    private String getProperty(Environment environment, String key, String defaultValue) {
        if (!propertyExists(environment, key)) {
            LOG.info("Encryptor config not found for property {}, using default value: {}", key, defaultValue);
        }
        return environment.getProperty(key, defaultValue);
    }

    private boolean propertyExists(Environment environment, String key) {
        return environment.getProperty(key) != null;
    }

    private String getRequiredProperty(Environment environment, String key) {
        if (!propertyExists(environment, key)) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return environment.getProperty(key);
    }

    private static final class LazyStringEncryptor implements StringEncryptor {
        private final Supplier<StringEncryptor> supplier;

        private LazyStringEncryptor(final Supplier<StringEncryptor> encryptorFactory) {
          supplier = new SingletonSupplier<StringEncryptor>(encryptorFactory);
        }

        @Override
        public String encrypt(String message) {
          return supplier.get().encrypt(message);
        }

        @Override
        public String decrypt(String encryptedMessage) {
          return supplier.get().decrypt(encryptedMessage);
        }
    }

    private static final class SingletonSupplier<T> implements Supplier<T> {

      private Supplier<T> singleton;
      private T value;

      private SingletonSupplier(final Supplier<T> original) {
        this.singleton = new Supplier<T>() {
          @Override
          public T get() {
            synchronized (original) {
              if(value == null) {
                value = original.get();
                singleton = new Supplier<T>() {
                  @Override
                  public T get() {
                    return value;
                  }
                };
              }
              return value;
            }
          }
        };
      }

      @Override
      public T get() {
        return singleton.get();
      }
    }

    private static interface Supplier<T> {
      T get();
    }
}
