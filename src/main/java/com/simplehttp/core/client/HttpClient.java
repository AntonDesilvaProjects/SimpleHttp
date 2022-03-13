package com.simplehttp.core.client;

import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;

@FunctionalInterface
public interface HttpClient {
    Response execute(Request request) throws Exception;
}
