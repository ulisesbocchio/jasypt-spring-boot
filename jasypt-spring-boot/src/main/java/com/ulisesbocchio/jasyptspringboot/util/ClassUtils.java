package com.ulisesbocchio.jasyptspringboot.util;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

public class ClassUtils {
    public static boolean isAssignable(ParameterizedTypeReference<?> type, Class<?> clazz) {
        return ResolvableType.forType(type).isAssignableFrom(clazz);
    }
}
