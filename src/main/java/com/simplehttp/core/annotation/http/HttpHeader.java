package com.simplehttp.core.annotation.http;

import com.simplehttp.core.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter level annotation used mark either single header value or a map of multiple headers. Ex:
 * <pre>
 *     {@code
 *     String getEntity(@HttpHeader("Authorization") String token);
 *     String getEntity(@HttpHeader Map<String, String> dynamicHeaders);
 *     String getEntity(@HttpHeader Map<String, List<String>> dynamicHeaders);
 *     }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpHeader {
    /**
     * Name of the HTTP header for single header values.
     */
    String value() default Constants.DEFAULT_STRING_VALUE;
}
