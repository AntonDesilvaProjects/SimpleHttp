package com.simplehttp.provider.spring;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Person {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String _id;
    private String name;
    private int age;
    private String city;
    private Map<String, Object> attributes;
}
