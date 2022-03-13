package com.simplehttp.core.annotation;

import com.simplehttp.core.Constants;
import com.simplehttp.core.client.http.HttpMultiValueMap;
import com.simplehttp.core.annotation.client.SimpleHttpClient;
import com.simplehttp.core.annotation.http.*;
import com.simplehttp.core.client.model.ClientMetadata;
import com.simplehttp.core.client.model.ClientMethodMetaData;
import com.simplehttp.core.client.model.ParameterMetaData;
import com.simplehttp.core.client.model.NamedParameterMetaData;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default annotation processor for a target SimpleHttp client class
 */
public class SimpleHttpDefaultAnnotationProcessor implements AnnotationProcessor {

    /**
     * {@inheritDoc}
     */
    public ClientMetadata extractClientMetadata(final Class<?> target) {
        final ClientMetadata.ClientMetadataBuilder clientMetadataBuilder = ClientMetadata.builder();
        // any simple http client must have SimpleHttpClient.class annotation
        if (!target.isAnnotationPresent(SimpleHttpClient.class)) {
            throw new IllegalArgumentException("Target class not annotated for Simple HTTP client");
        }
        // extract root level client meta data
        final SimpleHttpClient simpleHttpClientAnnotation = target.getAnnotation(SimpleHttpClient.class);
        final String name = Constants.DEFAULT_STRING_VALUE.equals(simpleHttpClientAnnotation.name()) ?
                target.getName() : simpleHttpClientAnnotation.name();
        final String host = simpleHttpClientAnnotation.host();
        clientMetadataBuilder.name(name);
        clientMetadataBuilder.host(host);

        // handle individual methods
        final Map<String, ClientMethodMetaData> methodNameToRequestMetadata = Arrays.stream(target.getMethods())
                .map(this::extractRequestMetadataFromMethod)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(methodMetaData -> methodMetaData.getMethod().getName(), Function.identity()));
        clientMetadataBuilder.methodNameToRequestTemplate(methodNameToRequestMetadata);

        // run validations
        ClientMetadata clientMetadata = clientMetadataBuilder.build();
        validateClientMetadata(clientMetadata);

        return clientMetadata;
    }

    /**
     * A method annotated with <code>@RequestAttribute</code> annotation in the target class represents an HTTP call.
     * Extract HTTP related metadata from the supplied method including HTTP method, URL, headers, query params, etc.
     *
     * @param method to extract HTTP request information
     * @return an ClientMethodMetaData object with extracted metadata from the method; empty if method not annotated for
     * http calls
     */
    private Optional<ClientMethodMetaData> extractRequestMetadataFromMethod(final Method method) {
        // the target class may have methods which are not annotated for making HTTP calls - ignore those
        if (!method.isAnnotationPresent(RequestAttribute.class)) {
            return Optional.empty();
        }

        final RequestAttribute requestAttributes = method.getAnnotation(RequestAttribute.class);

        // extract metadata from the root level annotation
        final ClientMethodMetaData.ClientMethodMetaDataBuilder builder = ClientMethodMetaData.builder();
        builder.method(method);
        builder.httpMethod(requestAttributes.httpMethod());
        // value and url are aliases - give preference to URL if defined
        builder.url(!Constants.DEFAULT_STRING_VALUE.equals(requestAttributes.url()) ? requestAttributes.url() :
                requestAttributes.value());
        // get the headers
        builder.headers(extractNameValuePairs(method, requestAttributes.headers()));
        // get the query parameter pairs
        builder.queryParams(extractNameValuePairs(method, requestAttributes.queryParams()));

        // parameters of the method can have additional metadata including headers, query params, request body, etc
        // go through all parameters of the method and extract any relevant metadata
        final List<ParameterMetaData> parameterMetaDataList = Arrays.stream(method.getParameters())
                .map(parameter -> extractParameterMetaData(method, parameter))
                .collect(Collectors.toList());
        builder.parameterMetaDataList(parameterMetaDataList);

        // get the return type
        builder.responseType(method.getGenericReturnType());

        return Optional.of(builder.build());
    }

    /**
     * The parameters of a method annotated with @RequestAttribute may contain additional information about the request
     * represented by that method including header, query param, body, etc info. Since these are parameters(and not
     * arguments), we can only extract metadata about possible future values rather than actual values.
     *
     * @param method
     * @param parameter
     * @return
     */
    private ParameterMetaData extractParameterMetaData(final Method method, final Parameter parameter) {
        ParameterMetaData methodParamMetaData = new ParameterMetaData();
        methodParamMetaData.setMethod(method);
        methodParamMetaData.setParameter(parameter);
        final Class<?> paramDataType = parameter.getType();
        if (parameter.isAnnotationPresent(QueryParam.class) || parameter.isAnnotationPresent(HttpHeader.class)) {
            final boolean isHeader = parameter.isAnnotationPresent(HttpHeader.class);
            if (Map.class.isAssignableFrom(paramDataType)) {
                // as long as it's a map, we can iterate through and build either a header or query param
                // map
                methodParamMetaData.setType(isHeader ? ParameterMetaData.Type.HTTP_HEADER_MAP :
                        ParameterMetaData.Type.QUERY_PARAM_MAP);
            } else {
                // single value of either a header or query param
                final NamedParameterMetaData namedArgument = new NamedParameterMetaData();
                String namedArgumentName;
                if (isHeader) {
                    final HttpHeader header = parameter.getAnnotation(HttpHeader.class);
                    namedArgument.setType(ParameterMetaData.Type.HTTP_HEADER);
                    namedArgumentName = header.value();
                } else {
                    final QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
                    namedArgument.setType(ParameterMetaData.Type.QUERY_PARAM);
                    namedArgumentName = queryParam.value();
                }
                if (Constants.DEFAULT_STRING_VALUE.equals(namedArgumentName)) {
                    throw new IllegalArgumentException(String.format("Unnamed %s at parameter %s#%s",
                            isHeader ? "header" : "query parameter", method.getName(), parameter.getName()));
                }
                namedArgument.setName(namedArgumentName);
                methodParamMetaData = namedArgument;
            }
        } else if (parameter.isAnnotationPresent(PathParam.class)) {
            final PathParam pathParam = parameter.getAnnotation(PathParam.class);
            NamedParameterMetaData namedParam = new NamedParameterMetaData();
            namedParam.setType(ParameterMetaData.Type.PATH_PARAM);
            namedParam.setName(pathParam.value());
            methodParamMetaData = namedParam;
        } else if (parameter.isAnnotationPresent(RequestBody.class)) {
            methodParamMetaData.setType(ParameterMetaData.Type.REQUEST_BODY);
        } else if (parameter.isAnnotationPresent(Url.class)) {
            methodParamMetaData.setType(ParameterMetaData.Type.URL);
        } else {
            // anything else should just be ignored
            methodParamMetaData.setType(ParameterMetaData.Type.NONE);
        }
        return methodParamMetaData;
    }

    private HttpMultiValueMap extractNameValuePairs(final Method method, final String[] nameValuePairs) {
        final HttpMultiValueMap nameToValuesMap = new HttpMultiValueMap();
        if (nameValuePairs != null) {
            for (String pair : nameValuePairs) {
                String[] splitPair = pair.split("=", 2);
                if (splitPair.length != 2) {
                    throw new InvalidParameterException(String.format("Invalid name-value pair at method %s: '%s'",
                            method.getName(), pair));
                }
                nameToValuesMap.add(splitPair[0], splitPair[1]);
            }
        }
        return nameToValuesMap;
    }

    private void validateClientMetadata(ClientMetadata clientMetadata) {
        // TODO: implement any necessary validations here
    }
}
