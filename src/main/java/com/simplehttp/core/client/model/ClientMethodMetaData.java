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
public class ClientMethodMetaData {
    private Method method;
    private HttpMethod httpMethod;
    private String url;
    private HttpMultiValueMap headers;
    private HttpMultiValueMap queryParams;
    private List<ParameterMetaData> parameterMetaDataList;
    private Type responseType;
}
