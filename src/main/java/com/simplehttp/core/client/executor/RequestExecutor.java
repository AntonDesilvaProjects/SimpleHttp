package com.simplehttp.core.client.executor;

import com.simplehttp.core.client.HttpClient;
import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.exception.SimpleHttpException;

import java.util.List;

/**
 * Implemented by a class that will execute the given request using the provided client and additional
 * request lifecycle interceptors.
 */
@FunctionalInterface
public interface RequestExecutor {

    /**
     * Executes the request using the provided client and additional request lifecycle interceptors.
     *
     * @param request request to execute
     * @param httpClient http client to execute the request with
     * @param requestInterceptorList request interceptors that are guaranteed to run in the provided order BEFORE
     *                               the request is executed
     * @param postRequestExecutorList response interceptors that are guaranteed to run in the provided order AFTER
     *                                the request is executed and some response is returned
     * @param errorHandlers error handlers to run sequentially if an error occurs while attempting to execute request
     * @return final response object which maybe null
     * @throws SimpleHttpException
     */
    Object execute(Request request,
                   HttpClient httpClient,
                   List<RequestInterceptor> requestInterceptorList,
                   List<ResponseInterceptor> postRequestExecutorList,
                   List<ErrorHandler> errorHandlers) throws SimpleHttpException;
}
