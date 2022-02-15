package com.simplehttp.core.client;

import com.simplehttp.core.client.model.ClientMetadata;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Proxy;
import java.util.Map;

public class ClientBuilder {
    private final RestTemplate restTemplate;
    private final AnnotationProcessor annotationProcessor;

    public ClientBuilder(RestTemplate restTemplate, AnnotationProcessor annotationProcessor) {
        this.restTemplate = restTemplate;
        this.annotationProcessor = annotationProcessor;
    }

    public <T> T buildClient(Class<T> target) {
        // use the target to generate a cache of request templates
        ClientMetadata clientMetadata = annotationProcessor.extractClientMetadata(target);
        // build client invocation handler
        ClientInvocationHandler clientInvocationHandler = new ClientInvocationHandler(null, clientMetadata);
        return (T) Proxy.newProxyInstance(ClientInvocationHandler.class.getClassLoader(),
                new Class[]{target}, clientInvocationHandler);
    }
}
