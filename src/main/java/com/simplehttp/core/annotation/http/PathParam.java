package com.simplehttp.core.annotation.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to represent parameters in the URL path. Ex:
 *  GET /employee/{id}/status
 *
 * `id` is a path parameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam {
    /**
     * The placeholder name of the path parameter
     */
    String value();
}
