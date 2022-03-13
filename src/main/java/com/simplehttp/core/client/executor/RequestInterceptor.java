package com.simplehttp.core.client.executor;

import com.simplehttp.core.client.HttpClient;
import com.simplehttp.core.client.model.Request;

/**
 * Implemented by a class that will run some pre-processing on a Request before it's executed. Typical use cases
 * include logging, Request modification, etc.
 */
@FunctionalInterface
public interface RequestInterceptor {
    /**
     * Process the supplied request. In case of chained interceptors, the processed request will be propagated to
     * the next interceptor.
     *
     * @param request request
     * @param httpClient the client that will execute the request
     * @return non-null processed request
     */
    Request process(Request request, HttpClient httpClient);
}
