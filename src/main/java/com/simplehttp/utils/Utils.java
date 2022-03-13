package com.simplehttp.utils;

import com.simplehttp.core.TypeReference;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

public class Utils {
    public static boolean isPathFragment(final String url) {
        return isNotEmpty(url) && !url.startsWith("http") && !url.startsWith("https");
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return isEmpty(str) || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static String stringify(Object s) {
        return Optional.ofNullable(s).map(Object::toString).orElse(null);
    }
}
