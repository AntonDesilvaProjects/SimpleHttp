package com.simplehttp.core.client;

import com.simplehttp.core.client.executor.*;
import com.simplehttp.core.client.http.HttpMultiValueMap;
import com.simplehttp.core.client.model.*;
import com.simplehttp.utils.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientInvocationHandler implements InvocationHandler {

    private final HttpClient httpClient;
    private final ClientMetadata clientMetadata;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseInterceptor> responseInterceptors;
    private final List<ErrorHandler> errorHandlers;

    private final RequestExecutor requestHandler;

    /**
     *
     * @param httpClient client that will be used to execute request
     * @param clientMetadata request meta data map which has info to build request objects
     */
    public ClientInvocationHandler(HttpClient httpClient,
                                   RequestExecutor requestHandler,
                                   ClientMetadata clientMetadata,
                                   List<RequestInterceptor> requestInterceptorList,
                                   List<ResponseInterceptor> postRequestExecutorList,
                                   List<ErrorHandler> errorHandlers) {
        this.httpClient = httpClient;
        this.clientMetadata = clientMetadata;
        this.requestHandler = requestHandler;
        this.requestInterceptors = requestInterceptorList;
        this.responseInterceptors = postRequestExecutorList;
        this.errorHandlers = errorHandlers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        final ClientMethodMetaData requestTemplate = Optional.ofNullable(
                clientMetadata.getMethodNameToRequestTemplate().get(methodName)).orElseThrow(
                        () -> new UnsupportedOperationException(String.format(
                                "The method '%s' is not decorated for Simple HTTP client request", methodName)));

        // combine the parameter metadata with the actual arguments
        final List<ParameterInfo> parameters = IntStream.range(0, requestTemplate.getParameterMetaDataList().size())
                .mapToObj(i -> ParameterInfo.builder()
                        .value(args[i])
                        .parameterMetaData(requestTemplate.getParameterMetaDataList().get(i))
                        .build())
                .collect(Collectors.toList());

        // build the request
        final Request request = buildRequest(requestTemplate, method, parameters);

        return requestHandler.execute(request, httpClient, requestInterceptors, responseInterceptors, errorHandlers);
    }

    private Request buildRequest(ClientMethodMetaData requestMetaData,
                                 Method method,
                                 List<ParameterInfo> parameters) {

        // group the parameter info list based on parameter type(e.g. HTTP_HEADER -> param_1, param_2)
        final Map<ParameterMetaData.Type, List<ParameterInfo>> parameterInfo = parameters.stream()
                .collect(Collectors.groupingBy(paramInfo ->
                        paramInfo.getParameterMetaData().getType()));

        final Request.RequestBuilder requestBuilder = Request.builder()
                .method(method)
                .parameters(parameters);

        final String url = getUrl(requestMetaData, parameterInfo);
        requestBuilder.url(url);
        requestBuilder.httpMethod(requestMetaData.getHttpMethod());
        // http headers
        final HttpMultiValueMap headers = getNamedParams(requestMetaData, parameterInfo,
                ParameterMetaData.Type.HTTP_HEADER);
        requestBuilder.headers(headers);
        // query parameters
        final HttpMultiValueMap queryParams = getNamedParams(requestMetaData, parameterInfo,
                ParameterMetaData.Type.QUERY_PARAM);
        requestBuilder.queryParams(queryParams);

        // request body
        final Object requestBody = getRequestBody(requestMetaData, parameterInfo);
        requestBuilder.body(requestBody);

        requestBuilder.responseType(requestMetaData.getResponseType());

        return requestBuilder.build();
    }

    /**
     * Extracts the URL from the request metadata. Following sequence is used to determine the final URL of the
     * request:
     * <ol>
     *  <li>If there is a URL specified as a method parameter, use that</li>
     *  <li>If there is a URL specified as part of the @RequestAttribute annotation, use that</li>
     *  <li>If none of the above, use the client's path from @SimpleHttpClient</li>
     *</ol>
     * For 1 & 2, if the specified value is a path fragment, then append to the client's path. Also replaces path
     * parameters with actual values.
     */
    private String getUrl(ClientMethodMetaData requestMetaData,
                          Map<ParameterMetaData.Type, List<ParameterInfo>> typeToParam) {
        // build the URL - we can get the URL in 3 ways
        String fullUrl = clientMetadata.getHost();

        // check for any parameter annotated with @Url - there can only be one
        List<ParameterInfo> paramsWithUrl = typeToParam.get(ParameterMetaData.Type.URL);
        if (Utils.isNotEmpty(paramsWithUrl) && paramsWithUrl.get(0).getValue() != null) {
            final Object rawUrlArgument = paramsWithUrl.get(0).getValue();
            fullUrl = joinUrl(fullUrl, Utils.stringify(rawUrlArgument));
        } else if (Utils.isNotEmpty(requestMetaData.getUrl())) {
            fullUrl = joinUrl(fullUrl, requestMetaData.getUrl());
        }

        // now we need to get the path variables and replace the placeholders in the URL
        List<ParameterInfo> pathVariableParams = Optional.ofNullable(typeToParam.get(ParameterMetaData.Type.PATH_PARAM))
                .orElse(List.of());
        for (ParameterInfo parameterInfo: pathVariableParams) {
            NamedParameterMetaData pathVariable = (NamedParameterMetaData) parameterInfo.getParameterMetaData();
            String pathVariableName = pathVariable.getName();
            String value = Utils.stringify(parameterInfo.getValue());
            if (Utils.isEmpty(value)) {
                throw new IllegalArgumentException(String.format("The value for path parameter '%s' is empty",
                        pathVariableName));
            }
            fullUrl = fullUrl.replaceAll("\\{\\s*" + pathVariableName + "\\s*\\}", value);
        }
        // TODO: Add some URL validation on this
        return fullUrl;
    }

    /**
     * Extracts the named parameters(headers or query params) from a request.
     */
    private HttpMultiValueMap getNamedParams(ClientMethodMetaData requestMetaData, Map<ParameterMetaData.Type,
            List<ParameterInfo>> typeToParam, ParameterMetaData.Type paramType) {
        // Named params(headers/query params) can come from two locations:
        final boolean isHeaders = paramType == ParameterMetaData.Type.HTTP_HEADER;
        final HttpMultiValueMap namedParams = new HttpMultiValueMap();

        // 1. @RequestAttribute annotation where a static list of namedParams is supplied
        HttpMultiValueMap valuesFromMethodAnnotation = isHeaders ? requestMetaData.getHeaders() :
                requestMetaData.getQueryParams();
        namedParams.addAll(valuesFromMethodAnnotation);

        // 2. Method arguments which can be either a single named value[e.g. getValue(@Header("Authorization") String token)]
        // or multiple namedParams as a map [e.g. getValue(@Header Map<String, String> headers)]

        // handle single nameParam values
        List<ParameterInfo> singleValues  = typeToParam.get(paramType);
        if (Utils.isNotEmpty(singleValues)) {
            singleValues.forEach(parameterInfo -> {
                final NamedParameterMetaData header = (NamedParameterMetaData) parameterInfo.getParameterMetaData();
                final Object rawValue = parameterInfo.getValue();
                if (rawValue != null) { // we will only add the named param if a value is provided
                    namedParams.add(header.getName(), rawValue.toString());
                }
            });
        }

        // handle get bulk namedParams as a map
        // note that the map can be <String, String> OR <String, Collection<?>>
        final ParameterMetaData.Type mapType = isHeaders ? ParameterMetaData.Type.HTTP_HEADER_MAP :
                ParameterMetaData.Type.QUERY_PARAM_MAP;
        final List<ParameterInfo> multipleHeaderMaps = typeToParam.get(mapType);
        if (Utils.isNotEmpty(multipleHeaderMaps)) {
            multipleHeaderMaps.forEach(parameterInfo -> {
                final Map<?, ?> map = (Map<?, ?>) parameterInfo.getValue();
                if (map != null) {
                    map.forEach((key, value) -> {
                        if (key != null && value != null) { // we will only add header if a name and value is present
                            final String headerName = key.toString();
                            if (value instanceof Collection<?> headerValueCollection) {
                                // header name is mapped to a collection so we have multiple header values
                                headerValueCollection.stream()
                                        .filter(Objects::nonNull)
                                        .forEach(headerValue -> namedParams.add(headerName, headerValue.toString()));
                            } else {
                                namedParams.add(headerName, value.toString());
                            }
                        }
                    });
                }
            });
        }
        return namedParams;
    }

    private Object getRequestBody(ClientMethodMetaData requestMetaData, Map<ParameterMetaData.Type, List<ParameterInfo>> typeToParam) {
        final List<ParameterInfo> requestBodyArgList = typeToParam.get(ParameterMetaData.Type.REQUEST_BODY);
        return Optional.ofNullable(requestBodyArgList)
                .filter(Utils::isNotEmpty)
                .map(argList -> argList.get(0).getValue())
                .orElse(null);
    }

    /**
     * If the supplied path is a path fragment, concatenate with rootUrl. Otherwise,
     * return the path as the full url
     */
    private String joinUrl(String rootUrl, String pathFragmentOrFullUrl) {
        String fullUrl = Utils.isEmpty(pathFragmentOrFullUrl) ? rootUrl : pathFragmentOrFullUrl;
        if (Utils.isPathFragment(pathFragmentOrFullUrl)) {
            fullUrl = rootUrl + (pathFragmentOrFullUrl.startsWith("/") ? pathFragmentOrFullUrl :
                    "/" + pathFragmentOrFullUrl);
        }
        return fullUrl;
    }
}
