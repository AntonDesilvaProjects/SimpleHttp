package com.simplehttp.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * An implementation of the "Super-Type Token" pattern to avoid erasure of parametric
 * types during the runtime.
 *
 * @param <T> the type to preserve such as <code>new TypeReference<String>(){}</code> or
 *           <code>new TypeReference<Map<String, List<String>>(){}</code>
 */
public abstract class TypeReference<T> {
    private final Type type;

    public TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * Get the Java Type associated with the generic parameters of this class.
     *
     * @return Type
     */
    public Type getType() {
        return type;
    }
}
