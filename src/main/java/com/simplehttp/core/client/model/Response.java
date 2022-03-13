package com.simplehttp.core.client.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Response {
    // TODO: add additional fields like http status, raw response, etc to make debugging easier
    private Object parsedResponse;
}
