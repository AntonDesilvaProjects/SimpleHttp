package com.simplehttp.core.client.executor;

import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by a class that will handle errors that occur during request execution.
 */
@FunctionalInterface
public interface ErrorHandler {
    /**
     * Handles an error that occurred while attempting to execute request. This method must either return a fallback
     * Response or throw a Runtime exception. Any returned fallback Response object will be propagated to subsequent
     * error handlers.
     *
     * @param request executed requested
     * @param response response maybe nullable
     * @param exception error that caused the request to fail
     * @return fallback Response
     */
    Response handleError(Request request, @Nullable Response response, Exception exception);
}
