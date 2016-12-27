package com.ulisesbocchio.jasyptspringboot.util;

import java.util.function.Supplier;

/**
 * Singleton initializer class that uses an internal supplier to supply the singleton instance. The supplier
 * originally checks whether the instanceSupplier
 * has been initialized or not, but after initialization the instance supplier is changed to avoid extra logic
 * execution.
 */
public final class Singleton<R> implements Supplier<R> {

    private boolean initialized = false;
    private volatile Supplier<R> instanceSupplier;

    public Singleton(final Supplier<R> original) {
        instanceSupplier = () -> {
            synchronized (original) {
                if (!initialized) {
                    final R singletonInstance = original.get();
                    instanceSupplier = () -> singletonInstance;
                    initialized = true;
                }
                return instanceSupplier.get();
            }
        };
    }

    @Override
    public R get() {
        return instanceSupplier.get();
    }
}
