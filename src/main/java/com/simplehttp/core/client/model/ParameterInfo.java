package com.simplehttp.core.client.model;

import lombok.Builder;
import lombok.Data;

/**
 * Class to combine parameter metadata with argument information.
 */
@Data
@Builder(toBuilder = true)
public class ParameterInfo {
    private ParameterMetaData parameterMetaData;
    private Object value;
}
