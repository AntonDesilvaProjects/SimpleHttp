package com.simplehttp.core.annotation.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameter level annotation used to mark URL path parameters. Ex:
 * <pre>
 *     {@code
 *      @RequestAttribute("/{id}")
 *      Person get(@PathParam("id") String id);
 *     }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam {
    /**
     * The placeholder name of the path parameter
     */
    String value();
}
