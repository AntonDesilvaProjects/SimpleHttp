package com.simplehttp.core.client.model;

import com.simplehttp.core.client.http.HttpMultiValueMap;
import com.simplehttp.core.client.http.HttpMethod;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

@Data
@Builder
public class Request {
    private String url;
    private HttpMethod httpMethod;
    private HttpMultiValueMap headers;
    private HttpMultiValueMap queryParams;
    private Object body;
    private Type responseType;

    private Method method;
    private List<ParameterInfo> parameters;
}
