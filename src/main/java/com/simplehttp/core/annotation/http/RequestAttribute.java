package com.simplehttp.core.annotation.http;

import com.simplehttp.core.Constants;
import com.simplehttp.core.client.http.HttpMethod;

import java.lang.annotation.*;

/**
 * A method level annotation that describes an HTTP request within a Simple Http Client target class.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAttribute {

    /**
     * The HTTP method of the request. Defaults to GET.
     */
    HttpMethod httpMethod() default HttpMethod.GET;

    /**
     * The path fragment or full URL of the request. This is an alias for
     * {@link RequestAttribute#url()}
     */
    String value() default Constants.DEFAULT_STRING_VALUE;

    /**
     * The path fragment or full URL of the request. This is an alias for {@link RequestAttribute#value()}
     */
    String url() default Constants.DEFAULT_STRING_VALUE;

    /**
     * List of static headers to include with the request where header name and value are delimited by an equal sign(=).
     *
     * Example:
     * <pre>
     *     @RequestAttribute(headers = {"Authorization=Bearer token", "region=sgp", "region=esp"})
     * </pre>
     */
    String[] headers() default {};

    /**
     * List of static query params to include with the request where query param name and value are delimited by an
     * equal sign(=).
     *
     * Example:
     * <pre>
     *     @RequestAttribute(queryParams = {"page=0", "pageSize=250"})
     * </pre>
     */
    String[] queryParams() default {};
}
