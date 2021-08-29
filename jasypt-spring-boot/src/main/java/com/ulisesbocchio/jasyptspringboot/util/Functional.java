package com.ulisesbocchio.jasyptspringboot.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Functional {
    public static <T> Function<T, T> tap(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    public static <T> Predicate<T> notNull() {
        return Objects::nonNull;
    }
}
