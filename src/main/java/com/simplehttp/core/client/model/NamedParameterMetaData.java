package com.simplehttp.core.client.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NamedParameterMetaData extends ParameterMetaData {
    private String name;
}
