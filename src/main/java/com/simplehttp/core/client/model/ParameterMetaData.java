package com.simplehttp.core.client.model;

import lombok.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Data
public class ParameterMetaData {

    public enum Type {
        URL, PATH_PARAM, QUERY_PARAM, QUERY_PARAM_MAP, HTTP_HEADER, HTTP_HEADER_MAP, REQUEST_BODY, NONE
    }

    private Type type;
    private Method method;
    private Parameter parameter;
}
