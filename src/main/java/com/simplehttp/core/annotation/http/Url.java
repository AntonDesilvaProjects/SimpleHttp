package com.simplehttp.core.annotation.http;

import com.simplehttp.core.annotation.client.SimpleHttpClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter level annotation to mark either a full request URL or path fragment. In case of fragments, value is
 * combined with URL provided with {@link SimpleHttpClient} to derive the final URL.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Url {
}
