package com.ulisesbocchio.jasyptspringboot.util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Singleton initializer class that uses an internal supplier to supply the singleton instance. The supplier
 * originally checks whether the instanceSupplier
 * has been initialized or not, but after initialization the instance supplier is changed to avoid extra logic
 * execution.
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public final class Singleton<R> implements Supplier<R> {

    private boolean initialized = false;
    private volatile Supplier<R> instanceSupplier;

    /**
     * <p>from.</p>
     *
     * @param original a {@link java.util.function.Supplier} object
     * @param <R> a R class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Singleton} object
     */
    public static <R> Singleton<R> from(final Supplier<R> original) {
        return new Singleton<>(original);
    }

    /**
     * <p>from.</p>
     *
     * @param original a {@link java.util.function.Function} object
     * @param arg0 a T object
     * @param <T> a T class
     * @param <R> a R class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Singleton} object
     */
    public static <T, R> Singleton<R> from(final Function<T, R> original, T arg0) {
        return fromLazy(original, () -> arg0);
    }

    /**
     * <p>from.</p>
     *
     * @param original a {@link java.util.function.BiFunction} object
     * @param arg0 a T object
     * @param arg1 a U object
     * @param <T> a T class
     * @param <U> a U class
     * @param <R> a R class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Singleton} object
     */
    public static <T, U, R> Singleton<R> from(final BiFunction<T, U, R> original, T arg0, U arg1) {
        return fromLazy(original, () -> arg0, () -> arg1);
    }

    /**
     * <p>fromLazy.</p>
     *
     * @param original a {@link java.util.function.Function} object
     * @param arg0Supplier a {@link java.util.function.Supplier} object
     * @param <T> a T class
     * @param <R> a R class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Singleton} object
     */
    public static <T, R> Singleton<R> fromLazy(final Function<T, R> original, Supplier<T> arg0Supplier) {
        return from(() -> original.apply(arg0Supplier.get()));
    }

    /**
     * <p>fromLazy.</p>
     *
     * @param original a {@link java.util.function.BiFunction} object
     * @param arg0Supplier a {@link java.util.function.Supplier} object
     * @param arg1Supplier a {@link java.util.function.Supplier} object
     * @param <T> a T class
     * @param <U> a U class
     * @param <R> a R class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Singleton} object
     */
    public static <T, U, R> Singleton<R> fromLazy(final BiFunction<T, U, R> original, Supplier<T> arg0Supplier, Supplier<U> arg1Supplier) {
        return from(() -> original.apply(arg0Supplier.get(), arg1Supplier.get()));
    }

    /**
     * <p>Constructor for Singleton.</p>
     *
     * @param original a {@link java.util.function.Supplier} object
     */
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

    /** {@inheritDoc} */
    @Override
    public R get() {
        return instanceSupplier.get();
    }
}
