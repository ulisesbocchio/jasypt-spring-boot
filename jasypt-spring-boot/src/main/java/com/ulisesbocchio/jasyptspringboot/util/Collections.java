package com.ulisesbocchio.jasyptspringboot.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Collections class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class Collections {
    /**
     * <p>concat.</p>
     *
     * @param listOne a {@link java.util.List} object
     * @param listTwo a {@link java.util.List} object
     * @param <T> a T class
     * @return a {@link java.util.List} object
     */
    public static <T> List<T> concat(List<T> listOne, List<T> listTwo) {
        return Stream.concat(listOne.stream(), listTwo.stream())
                .collect(Collectors.toList());
    }
}
