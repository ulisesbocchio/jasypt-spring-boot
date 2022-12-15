package com.ulisesbocchio.jasyptspringboot.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>Iterables class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class Iterables {

    /**
     * <p>decorate.</p>
     *
     * @param source a {@link java.lang.Iterable} object
     * @param transform a {@link java.util.function.Function} object
     * @param filter a {@link java.util.function.Predicate} object
     * @param <U> a U class
     * @param <T> a T class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Iterables.IterableDecorator} object
     */
    static public <U, T> IterableDecorator<U, T> decorate(Iterable<U> source, Function<U, T> transform, Predicate<U> filter) {
        return new IterableDecorator<>(source, transform, filter);
    }

    /**
     * <p>transform.</p>
     *
     * @param source a {@link java.lang.Iterable} object
     * @param transform a {@link java.util.function.Function} object
     * @param <U> a U class
     * @param <T> a T class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Iterables.IterableDecorator} object
     */
    static public <U, T> IterableDecorator<U, T> transform(Iterable<U> source, Function<U, T> transform) {
        return new IterableDecorator<>(source, transform, v -> true);
    }

    /**
     * <p>filter.</p>
     *
     * @param source a {@link java.lang.Iterable} object
     * @param filter a {@link java.util.function.Predicate} object
     * @param <T> a T class
     * @return a {@link com.ulisesbocchio.jasyptspringboot.util.Iterables.IterableDecorator} object
     */
    static public <T> IterableDecorator<T, T> filter(Iterable<T> source, Predicate<T> filter) {
        return new IterableDecorator<>(source, Function.identity(), filter);
    }

    public static class IterableDecorator<U, T> implements Iterable<T> {
        private final Function<U, T> transform;
        private final Predicate<U> filter;
        private final Iterable<U> source;

        IterableDecorator(Iterable<U> source, Function<U, T> transform, Predicate<U> filter) {
            this.source = source;
            this.transform = transform;
            this.filter = filter;
        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorDecorator<>(this.source.iterator(), this.transform, this.filter);
        }
    }

    public static class IteratorDecorator<U, T> implements Iterator<T> {

        private final Iterator<U> source;
        private final Function<U, T> transform;
        private final Predicate<U> filter;
        private T next = null;

        public IteratorDecorator(Iterator<U> source, Function<U, T> transform, Predicate<U> filter) {
            this.source = source;
            this.transform = transform;
            this.filter = filter;
        }

        public boolean hasNext() {
            this.maybeFetchNext();
            return next != null;
        }

        public T next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            T val = next;
            next = null;
            return val;
        }

        private void maybeFetchNext() {
            if (next == null) {
                if (source.hasNext()) {
                    U val = source.next();
                    if (filter.test(val)) {
                        next = transform.apply(val);
                    }
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}


