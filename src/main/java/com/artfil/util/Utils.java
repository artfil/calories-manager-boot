package com.artfil.util;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class Utils {
    public static <T extends Comparable<T>> boolean isBetweenHalfOpen(T value, @Nullable T start, @Nullable T end) {
        return (start == null || value.compareTo(start) >= 0) && (end == null || value.compareTo(end) < 0);
    }
}