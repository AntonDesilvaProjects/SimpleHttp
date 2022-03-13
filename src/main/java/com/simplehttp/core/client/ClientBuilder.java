package com.simplehttp.core.client;

import com.simplehttp.core.annotation.AnnotationProcessor;
import com.simplehttp.core.annotation.SimpleHttpDefaultAnnotationProcessor;
import com.simplehttp.core.client.executor.*;
import com.simplehttp.core.client.model.ClientMetadata;
import com.simplehttp.httpclient.spring.RestTemplateHttpClient;

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
        this.httpClient = new RestTemplateHttpClient();
        this.annotationProcessor = new SimpleHttpDefaultAnnotationProcessor();
        this.executionHandler = new DefaultRequestExecutor();
        this.requestInterceptors = new ArrayList<>();
        this.responseInterceptors = new ArrayList<>();
        this.errorHandlers = new ArrayList<>();
    }

    /**
     * The HTTP client used to execute Requests.
     *
     * @param httpClient implementation of {@link HttpClient}
     * @return ClientBuilder
     */
    public ClientBuilder withHttpClient(HttpClient httpClient) {
        this.httpClient = Optional.ofNullable(httpClient)
                .orElseThrow(() -> new IllegalArgumentException("HTTP client cannot be null!"));
        return this;
    }

    /**
     * The ExecutionHandler used to orchestrate HTTP requests.
     *
     * @param executionHandler implementation of {@link RequestExecutor}
     * @return ClientBuilder
     */
    public ClientBuilder withExecutionHandler(RequestExecutor executionHandler) {
        this.executionHandler = Optional.ofNullable(executionHandler)
                .orElseThrow(() -> new IllegalArgumentException("Execution handler cannot be null!"));
        return this;
    }

    /**
     * The annotation processor used to parse and construct HTTP Requests from a target
     * class.
     *
     * @param annotationProcessor implementation of {@link AnnotationProcessor}
     * @return ClientBuilder
     */
    public ClientBuilder withAnnotationProcessor(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = Optional.ofNullable(annotationProcessor)
                .orElseThrow(() -> new IllegalArgumentException("Annotation procesor cannot be null!"));
        return this;
    }

    /**
     * List of Request Interceptors that will be executed in order.
     *
     * @param interceptors a list of {@link RequestInterceptor}
     * @return ClientBuilder
     */
    public ClientBuilder withRequestInterceptors(List<RequestInterceptor> interceptors) {
        if (interceptors != null) {
            this.requestInterceptors.addAll(interceptors);
        }
        return this;
    }

    /**
     * Add a request interceptor
     *
     * @param interceptor implementation of {@link RequestInterceptor}
     * @return ClientBuilder
     */
    public ClientBuilder withRequestInterceptor(RequestInterceptor interceptor) {
        if (interceptor != null) {
            this.requestInterceptors.add(interceptor);
        }
        return this;
    }

    /**
     * List of Response Interceptors that will be executed in order.
     *
     * @param interceptors a list of {@link ResponseInterceptor}
     * @return ClientBuilder
     */
    public ClientBuilder withResponseInterceptors(List<ResponseInterceptor> interceptors) {
        if (interceptors != null) {
            this.responseInterceptors.addAll(interceptors);
        }
        return this;
    }

    /**
     * Add a response interceptor
     *
     * @param interceptor implementation of {@link ResponseInterceptor}
     * @return ClientBuilder
     */
    public ClientBuilder withResponseInterceptor(ResponseInterceptor interceptor) {
        if (interceptor != null) {
            this.responseInterceptors.add(interceptor);
        }
        return this;
    }

    /**
     * List of error handler to execute in event of an error during request
     * execution.
     *
     * @param errorHandlers a list of {@link ErrorHandler}
     * @return ClientBuilder
     */
    public ClientBuilder withErrorHandlers(List<ErrorHandler> errorHandlers) {
        if (errorHandlers != null) {
            this.errorHandlers.addAll(errorHandlers);
        }
        return this;
    }

    /**
     * An Error handler to execute in event of an error during request
     * execution.
     *
     * @param errorHandler an implementation of {@link ErrorHandler}
     * @return ClientBuilder
     */
    public ClientBuilder withErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler != null) {
            this.errorHandlers.add(errorHandler);
        }
        return this;
    }

    /**
     * Builds client for the specified target class.
     *
     * @param target class annotated with {@link com.simplehttp.core.annotation.client.SimpleHttpClient} that
     *               represents a collection of requests.
     * @param <T>
     * @return client for the target class
     */
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
