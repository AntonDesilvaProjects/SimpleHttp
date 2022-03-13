package com.simplehttp.core.exception;

import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.executor.RequestInterceptor;
import lombok.Getter;

@Getter
public class RequestInterceptorException extends SimpleHttpException {
    private final RequestInterceptor processor;

    public RequestInterceptorException(String message, Request request) {
        super(message, request);
        this.processor = null;
    }

    public RequestInterceptorException(String message, Request request, RequestInterceptor processor) {
        super(message, request);
        this.processor = processor;
    }

    public RequestInterceptorException(String message, Request request, RequestInterceptor processor, Exception exception) {
        super(message, request, exception);
        this.processor = processor;
    }
}
