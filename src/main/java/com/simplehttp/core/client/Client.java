package com.simplehttp.core.client;

import com.simplehttp.core.client.model.Request;
import com.simplehttp.core.client.model.Response;

public interface Client {
    Response execute(Request request);
}
