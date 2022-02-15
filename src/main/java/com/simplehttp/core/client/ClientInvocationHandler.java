package com.simplehttp.core.client;

import com.simplehttp.core.HttpMultiValueMap;
import com.simplehttp.core.annotation.http.QueryParam;
import com.simplehttp.core.client.model.*;
import com.simplehttp.utils.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.simplehttp.utils.Utils.isPathFragment;

public class ClientInvocationHandler implements InvocationHandler {

    private final Client client;
    private final ClientMetadata clientMetadata;

    /**
     *
     * @param client client that will be used to execute request
     * @param clientMetadata request meta data map which has info to build request objects
     */
    public ClientInvocationHandler(Client client, ClientMetadata clientMetadata) {
        this.client = client;
        this.clientMetadata = clientMetadata;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        final ClientMethodMetaData requestTemplate = clientMetadata.getMethodNameToRequestTemplate().get(methodName);
        if (requestTemplate == null) {
            throw new UnsupportedOperationException(String.format("The method %s is not valid Simple HTTP client method", methodName));
        }
        // combine the parameter metadata with the actual arguments
        List<ParameterInfo> parameterInfoList = IntStream.range(0, requestTemplate.getParameterMetaDataList().size())
                .mapToObj(i -> ParameterInfo.builder()
                        .value(args[i])
                        .parameterMetaData(requestTemplate.getParameterMetaDataList().get(i))
                        .build())
                .collect(Collectors.toList());
        // group the parameter info list based on parameter type
        Map<ParameterMetaData.Type, List<ParameterInfo>> typeToParamInfo = parameterInfoList.stream()
                .collect(Collectors.groupingBy(paramInfo -> paramInfo.getParameterMetaData().getType()));

        // combine the actual arguments with parameter metadata
        // do group by parameter type( HTTP_HEADER -> param_1, param_2)

        // build the request
        Request request = buildRequest(requestTemplate, typeToParamInfo);
        Response response;
        try {
            response = client.execute(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response.getParsedResponse();
    }

    private Request buildRequest(ClientMethodMetaData requestMetaData,
                                 Map<ParameterMetaData.Type,
                                 List<ParameterInfo>> parameterInfo) {

        final Request.RequestBuilder requestBuilder = Request.builder();
        final String url = getUrl(requestMetaData, parameterInfo);
        requestBuilder.url(url);
        requestBuilder.httpMethod(requestMetaData.getHttpMethod());
        // http headers
        final HttpMultiValueMap headers = getNamedParams(requestMetaData, parameterInfo, ParameterMetaData.Type.HTTP_HEADER);
        requestBuilder.headers(headers);
        // query parameters
        final HttpMultiValueMap queryParams = getNamedParams(requestMetaData, parameterInfo, ParameterMetaData.Type.QUERY_PARAM);
        requestBuilder.queryParams(queryParams);
        requestBuilder.responseType(requestMetaData.getResponseType());

        return requestBuilder.build();
    }

    /**
     * Extracts the URL from the request metadata. Following sequence is used to determine the final URL of the
     * request:
     * 1. If there is a URL specified as a method parameter, use that
     * 2. If there is a URL specified as part of the RequestAttribute annotation, use that
     * 3. If none of the above, use just the client's path
     *
     */
    private String getUrl(ClientMethodMetaData requestMetaData, Map<ParameterMetaData.Type, List<ParameterInfo>> typeToParam) {
        // build the URL - we can get the URL in 3 ways
        String fullUrl = clientMetadata.getHost();

        // check for any parameter annotated with @Url - there can only be one
        List<ParameterInfo> paramsWithUrl = typeToParam.get(ParameterMetaData.Type.URL);
        if (Utils.isNotEmpty(paramsWithUrl) && paramsWithUrl.get(0).getValue() != null) {
            final Object rawUrlArgument = paramsWithUrl.get(0).getValue();
            final String urlArgString = rawUrlArgument instanceof String ? (String) rawUrlArgument :
                    rawUrlArgument.toString();
            fullUrl = joinUrl(fullUrl, urlArgString);
        } else if (Utils.isNotEmpty(requestMetaData.getUrl())) {
            fullUrl = joinUrl(fullUrl, requestMetaData.getUrl());
        }

        // now we need to get the path variables and replace the placeholders in the URL
        List<ParameterInfo> pathVariableParams = typeToParam.get(ParameterMetaData.Type.PATH_PARAM);
        for (ParameterInfo parameterInfo: pathVariableParams) {
            NamedParameterMetaData queryParam = (NamedParameterMetaData) parameterInfo.getParameterMetaData();
            String name = queryParam.getName();
            Object rawValue = parameterInfo.getValue();
            String value = rawValue instanceof String ? (String) rawValue : rawValue.toString();
            fullUrl = fullUrl.replaceAll("\\{\\s*" + name + "\\s*\\}", value);
        }

        if (Utils.isPathFragment(fullUrl)) { // TODO: Needs better url validation here!
            throw new IllegalArgumentException("Unable to find valid request URL. Found: " + fullUrl);
        }

        return fullUrl;
    }

    /**
     * Extracts the headers from the request method
     * @param requestMetaData
     * @param typeToParam
     * @return
     */
    private HttpMultiValueMap getHeaders(ClientMethodMetaData requestMetaData, Map<ParameterMetaData.Type, List<ParameterInfo>> typeToParam) {
        // Headers can come from two locations:
        final HttpMultiValueMap headers = new HttpMultiValueMap();
        // 1. @RequestAttribute annotation where a static list of headers is supplied
        HttpMultiValueMap headersFromMethodAnnotation = requestMetaData.getHeaders();
        headers.addAll(headersFromMethodAnnotation);

        // 2. Method arguments which can be either a single header or multiple headers as a map
        List<ParameterInfo> singleValueHeaders = typeToParam.get(ParameterMetaData.Type.HTTP_HEADER);
        singleValueHeaders.forEach(parameterInfo -> {
            final NamedParameterMetaData header = (NamedParameterMetaData) parameterInfo.getParameterMetaData();
            final Object rawValue = parameterInfo.getValue();
            if (rawValue != null) { // we will only add the header if a value is provided
                headers.add(header.getName(), rawValue.toString());
            }
        });

        final List<ParameterInfo> multipleHeaderMaps = typeToParam.get(ParameterMetaData.Type.HTTP_HEADER_MAP);
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
                                    .forEach(headerValue -> headers.add(headerName, headerValue.toString()));
                        } else {
                            headers.add(headerName, value.toString());
                        }
                    }
                });
            }
        });

        return headers;
    }

    /**
     * Extracts the named parameters(headers or query params) from a request.
     *
     * @param requestMetaData
     * @param typeToParam
     * @param paramType
     * @return
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

        // 2. Method arguments which can be either a single header or multiple namedParams as a map
        List<ParameterInfo> singleValues  = typeToParam.get(paramType);
        singleValues.forEach(parameterInfo -> {
            final NamedParameterMetaData header = (NamedParameterMetaData) parameterInfo.getParameterMetaData();
            final Object rawValue = parameterInfo.getValue();
            if (rawValue != null) { // we will only add the header if a value is provided
                namedParams.add(header.getName(), rawValue.toString());
            }
        });

        final ParameterMetaData.Type mapType = isHeaders ? ParameterMetaData.Type.HTTP_HEADER_MAP :
                ParameterMetaData.Type.QUERY_PARAM_MAP;
        final List<ParameterInfo> multipleHeaderMaps = typeToParam.get(mapType);
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

        return namedParams;
    }



    /**
     * If the supplied path is a path fragment, concatenate with rootUrl. Otherwise,
     * return the path as the full url
     */
    private String joinUrl(String rootUrl, String pathFragmentOrFullUrl) {
        String fullUrl = pathFragmentOrFullUrl;
        if (Utils.isPathFragment(pathFragmentOrFullUrl)) {
            fullUrl = rootUrl + (pathFragmentOrFullUrl.startsWith("/") ? pathFragmentOrFullUrl :
                    "/" + pathFragmentOrFullUrl);
        }
        return fullUrl;
    }

    public static void main(String... args) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(null, null);
        System.out.println(headers);
    }
}
