package com.ulisesbocchio.jasyptspringboot.encryptor;

import org.jasypt.encryption.StringEncryptor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class PooledStringEncryptor implements StringEncryptor {

    private final int size;
    private final StringEncryptor[] pool;
    private final AtomicInteger roundRobin;

    public PooledStringEncryptor(int size, Supplier<StringEncryptor> encryptorFactory) {
        this.size = size;
        this.pool = IntStream.range(0, this.size).boxed().map(v -> {
            StringEncryptor encryptor = encryptorFactory.get();
            if (encryptor instanceof ThreadSafeStringEncryptor) {
                return encryptor;
            }
            return new ThreadSafeStringEncryptor(encryptor);
        }).toArray(StringEncryptor[]::new);
        this.roundRobin = new AtomicInteger();
    }

    private <T> T robin(Function<StringEncryptor, T> producer) {
        int position = this.roundRobin.getAndUpdate(value ->  (value + 1) % this.size);
        return producer.apply(this.pool[position]);

    }

    @Override
    public String encrypt(String message) {
        return robin(e -> e.encrypt(message));
    }

    @Override
    public String decrypt(String encryptedMessage) {
        return robin(e -> e.decrypt(encryptedMessage));
    }

    public static class ThreadSafeStringEncryptor implements StringEncryptor {
        private final StringEncryptor delegate;

        public ThreadSafeStringEncryptor(StringEncryptor delegate) {
            this.delegate = delegate;
        }

        @Override
        public synchronized String encrypt(String message) {
            return delegate.encrypt(message);
        }

        @Override
        public synchronized String decrypt(String encryptedMessage) {
            return delegate.decrypt(encryptedMessage);
        }
    }
}
