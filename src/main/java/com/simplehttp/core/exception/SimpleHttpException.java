package com.simplehttp.core.exception;

import com.simplehttp.core.client.model.Request;
import lombok.Getter;

@Getter
public class SimpleHttpException extends RuntimeException {

    private final Request request;

    public SimpleHttpException(String message, Request request) {
        super(message);
        this.request = request;
    }

    public SimpleHttpException(String message, Request request, Exception exception) {
        super(message, exception);
        this.request = request;
    }
}
