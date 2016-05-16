package com.ulisesbocchio.jasyptspringboot.encryptor;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.Environment;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * String encryptor that delays pulling configuration properties to configure the encryptor until the moment when the
 * first encrypted property is retrieved. Thus allowing for late retrieval of
 * configuration when all property sources have been established, and avoids missing configuration properties errors
 * when no encrypted properties are present in configuration files.
 */
public final class LazyStringEncryptor implements StringEncryptor {

    private final Function<Environment, StringEncryptor> supplier;
    private Environment environment;

    public LazyStringEncryptor(final Function<Environment, StringEncryptor> encryptorFactory, Environment environment) {
        supplier = new SingletonFunction<>(encryptorFactory);
        this.environment = environment;
    }

    @Override
    public String encrypt(String message) {
        return supplier.apply(environment).encrypt(message);
    }

    @Override
    public String decrypt(String encryptedMessage) {
        return supplier.apply(environment).decrypt(encryptedMessage);
    }

    /**
     * Singleton initializer class that uses an internal supplier to supply the singleton instance. The supplier
     * originally checks whether the instanceFunction
     * has been initialized or not, but after initialization the instance supplier is changed to avoid extra logic
     * execution.
     */
    private static final class SingletonFunction<T, R> implements Function<T, R> {

        private boolean initialized = false;
        private volatile Function<T, R> instanceFunction;

        private SingletonFunction(final Function<T, R> original) {
            this.instanceFunction = t -> {
                synchronized (original) {
                    if (!initialized) {
                        final R singletonInstance = original.apply(t);
                        instanceFunction = t1 -> {
                            return singletonInstance;
                        };
                        initialized = true;
                    }
                    return instanceFunction.apply(t);
                }
            };
        }

        @Override
        public R apply(T t) {
            return instanceFunction.apply(t);
        }
    }
}
