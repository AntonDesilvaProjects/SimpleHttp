package com.simplehttp.core.client.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ClientMetadata {
    private String name;
    private String host;
    private Map<String, ClientMethodMetaData> methodNameToRequestTemplate;
}
