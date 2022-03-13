package com.simplehttp.core.client;

import com.simplehttp.core.annotation.AnnotationProcessor;
import com.simplehttp.core.annotation.SimpleHttpDefaultAnnotationProcessor;
import com.simplehttp.core.client.executor.*;
import com.simplehttp.core.client.model.ClientMetadata;
import com.simplehttp.provider.spring.RestTemplateClient;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientBuilder {

    private HttpClient httpClient;
    private AnnotationProcessor annotationProcessor;
    private RequestExecutor executionHandler;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseInterceptor> responseInterceptors;
    private final List<ErrorHandler> errorHandlers;

    public ClientBuilder() {
        this.httpClient = new RestTemplateClient();
        this.annotationProcessor = new SimpleHttpDefaultAnnotationProcessor();
        this.executionHandler = new DefaultRequestExecutor();
        this.requestInterceptors = new ArrayList<>();
        this.responseInterceptors = new ArrayList<>();
        this.errorHandlers = new ArrayList<>();
    }

    public ClientBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public ClientBuilder withExecutionHandler(RequestExecutor executionHandler) {
        this.executionHandler = executionHandler;
        return this;
    }

    public ClientBuilder withAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = annotationProcessor;
        return this;
    }

    public ClientBuilder withRequestInterceptors(List<RequestInterceptor> interceptors) {
        if (interceptors != null) {
            this.requestInterceptors.addAll(interceptors);
        }
        return this;
    }

    public ClientBuilder withRequestInterceptor(RequestInterceptor interceptor) {
        if (interceptor != null) {
            this.requestInterceptors.add(interceptor);
        }
        return this;
    }

    public ClientBuilder withResponseInterceptors(List<ResponseInterceptor> interceptors) {
        if (interceptors != null) {
            this.responseInterceptors.addAll(interceptors);
        }
        return this;
    }

    public ClientBuilder withResponseInterceptor(ResponseInterceptor interceptor) {
        if (interceptor != null) {
            this.responseInterceptors.add(interceptor);
        }
        return this;
    }

    public ClientBuilder withErrorHandlers(List<ErrorHandler> errorHandlers) {
        if (errorHandlers != null) {
            this.errorHandlers.addAll(errorHandlers);
        }
        return this;
    }

    public ClientBuilder withErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler != null) {
            this.errorHandlers.add(errorHandler);
        }
        return this;
    }

    public <T> T buildClient(Class<T> target) {
        // use the target to generate a cache of request templates
        ClientMetadata clientMetadata = annotationProcessor.extractClientMetadata(target);

        // get the request executor
        RequestExecutor executor = Optional.ofNullable(this.executionHandler)
                .orElse(new DefaultRequestExecutor());

        // build client invocation handler
        ClientInvocationHandler clientInvocationHandler = new ClientInvocationHandler(httpClient, executor, clientMetadata,
                requestInterceptors, responseInterceptors, errorHandlers);
        return (T) Proxy.newProxyInstance(ClientInvocationHandler.class.getClassLoader(),
                new Class[]{target}, clientInvocationHandler);
    }
}
