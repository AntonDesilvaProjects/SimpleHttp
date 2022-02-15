package com.simplehttp.core.annotation.http;

import com.simplehttp.core.Constants;
import com.simplehttp.core.client.http.HttpMethod;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestAttribute {
    HttpMethod httpMethod() default HttpMethod.GET;
    String value() default Constants.DEFAULT_STRING_VALUE; // path or full URL (if it starts with http or https, then treat as full url)
    String url() default Constants.DEFAULT_STRING_VALUE; // path or full URL
    String[] headers() default {};
    String[] queryParams() default {};
}
