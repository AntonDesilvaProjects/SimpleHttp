package com.simplehttp.core.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to represent paramters in the query string. Ex:
 * GET /employees?page={page_num}
 * `page_num` is a query parameter
 * When used, queryParam("page_num) 23 will auto-generate page_num=12
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {
    String value() default "";
}
