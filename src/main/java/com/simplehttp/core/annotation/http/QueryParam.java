package com.simplehttp.core.annotation.http;

import com.simplehttp.core.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter level annotation to mark query params that will be appended to URL or a map of multiple query params. Ex:
 * <pre>
 *     {@code
 *      List<Person> list(@QueryParam("page") int page, @QueryParam("pageSize") int pageSize);
 *      List<Person> list(@QueryParam Map<String, String> dynamicQueryParams);
 *      List<Person> list(@QueryParam Map<String, List<String>> dynamicQueryParams);
 *     }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {
    /**
     * Name of the query param for single query param values.
     */
    String value() default Constants.DEFAULT_STRING_VALUE;
}
