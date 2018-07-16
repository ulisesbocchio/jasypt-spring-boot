package com.ulisesbocchio.jasyptspringboot.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Functional {
    public static <T> Function<T, T> tap(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }
}
