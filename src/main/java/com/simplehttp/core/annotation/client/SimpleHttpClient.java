package com.simplehttp.core.annotation.client;

import com.simplehttp.core.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Root level annotation required to mark a target class as a Simple HTTP Client.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleHttpClient {
    /**
     * The name of the client. If a name is not provided, the class name will be used.
     */
    String name() default Constants.DEFAULT_STRING_VALUE;

    /**
     * The host value of the client. The host provided here will be used by default for all client methods
     * which are annotated with path fragments.
     */
    String host() default Constants.DEFAULT_STRING_VALUE;
}
