package com.ulisesbocchio.jasyptspringboot.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>Functional class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class Functional {
    /**
     * <p>tap.</p>
     *
     * @param consumer a {@link java.util.function.Consumer} object
     * @param <T> a T class
     * @return a {@link java.util.function.Function} object
     */
    public static <T> Function<T, T> tap(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    /**
     * <p>notNull.</p>
     *
     * @param <T> a T class
     * @return a {@link java.util.function.Predicate} object
     */
    public static <T> Predicate<T> notNull() {
        return Objects::nonNull;
    }
}
