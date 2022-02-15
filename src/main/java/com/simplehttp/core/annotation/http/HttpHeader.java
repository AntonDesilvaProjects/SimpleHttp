package com.simplehttp.core.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpHeader {
    /**
     * Name of the HTTP header. If omitted, header's value(or its string representation) will  be used as
     * the name.
     */
    String value() default "";
}
