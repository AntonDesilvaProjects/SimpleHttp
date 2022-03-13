package com.simplehttp.core.client;

import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;

/**
 * Implemented by a HTTP client.
 */
@FunctionalInterface
public interface HttpClient {

    /**
     * Executes the provided Request and returns the Response.
     * @param request Request to execute
     * @return Response from request
     * @throws Exception errors thrown during Request execution
     */
    Response execute(Request request) throws Exception;
}
