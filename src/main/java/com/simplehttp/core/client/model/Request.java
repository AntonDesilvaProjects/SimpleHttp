package com.simplehttp.core.client.model;

import com.simplehttp.core.HttpMultiValueMap;
import com.simplehttp.core.client.http.HttpMethod;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@Builder
public class Request {
    private String url;
    private HttpMethod httpMethod;
    private HttpMultiValueMap headers;
    private HttpMultiValueMap queryParams;
    private Object body;
    private Class<?> responseType;

    private Method method;
}
