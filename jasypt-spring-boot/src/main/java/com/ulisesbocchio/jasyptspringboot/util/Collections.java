package com.ulisesbocchio.jasyptspringboot.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Collections {
    public static <T> List<T> concat(List<T> listOne, List<T> listTwo) {
        return Stream.concat(listOne.stream(), listTwo.stream())
                .collect(Collectors.toList());
    }
}
