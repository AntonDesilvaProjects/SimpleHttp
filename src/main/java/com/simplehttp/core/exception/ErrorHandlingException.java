package com.simplehttp.core.exception;

import com.simplehttp.core.client.executor.ErrorHandler;
import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;
import lombok.Getter;

@Getter
public class ErrorHandlingException extends SimpleHttpException {

    private final Exception requestException;
    private final ErrorHandler errorHandler;
    private final Response response;

    public ErrorHandlingException(String message,
                                  Request request,
                                  Exception requestException,
                                  Response response,
                                  ErrorHandler errorHandler) {
        super(message, request);
        this.requestException = requestException;
        this.response = response;
        this.errorHandler = errorHandler;
    }

    public ErrorHandlingException(String message,
                                  Request request,
                                  Exception requestException,
                                  Response response,
                                  ErrorHandler errorHandler,
                                  Exception exception) {

        super(message, request, exception);
        this.requestException = requestException;
        this.response = response;
        this.errorHandler = errorHandler;
    }
}
