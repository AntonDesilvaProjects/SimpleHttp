package com.simplehttp.core.exception;

import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.executor.ResponseInterceptor;
import com.simplehttp.core.client.model.Response;
import lombok.Getter;

@Getter
public class ResponseInterceptorException extends SimpleHttpException {

    private final ResponseInterceptor processor;
    private final Response response;

    public ResponseInterceptorException(String message, Request request, Response response) {
        super(message, request);
        this.response = response;
        this.processor = null;
    }

    public ResponseInterceptorException(String message, Request request, Response response, ResponseInterceptor processor) {
        super(message, request);
        this.response = response;
        this.processor = processor;
    }

    public ResponseInterceptorException(String message, Request request, Response response, ResponseInterceptor processor, Exception exception) {
        super(message, request, exception);
        this.response = response;
        this.processor = processor;
    }
}
