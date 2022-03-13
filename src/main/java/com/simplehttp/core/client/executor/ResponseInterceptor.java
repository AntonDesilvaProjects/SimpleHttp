package com.simplehttp.core.client.executor;

import com.simplehttp.core.client.HttpClient;
import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;

/**
 * Implemented by a class that will run some processing on a Response returned from a Request.
 */
@FunctionalInterface
public interface ResponseInterceptor {
    /**
     * Process the supplied response. In case of chained interceptors, the processed response will be propagated to
     * the next interceptor.
     *
     * @param response response from the request execution
     * @param request executed request
     * @param httpClient client used to execute request
     * @return non-null processed Response
     */
    Response process(Response response, Request request, HttpClient httpClient);
}
