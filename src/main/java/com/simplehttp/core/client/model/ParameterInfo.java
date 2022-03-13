package com.simplehttp.core.client.model;

import lombok.Builder;
import lombok.Data;

/**
 * Composite object to store a parameter's metadata alongside actual argument value.
 */
@Data
@Builder(toBuilder = true)
public class ParameterInfo {
    private ParameterMetaData parameterMetaData;
    private Object value;
}
