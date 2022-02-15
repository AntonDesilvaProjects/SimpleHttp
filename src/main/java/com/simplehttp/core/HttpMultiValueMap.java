package com.simplehttp.core;

import java.util.*;

public class HttpMultiValueMap {

    private final Map<String, List<String>> nameToValuesMap;

    public HttpMultiValueMap() {
        this(new LinkedHashMap<>());
    }

    public HttpMultiValueMap(Map<String, List<String>> headers) {
        this.nameToValuesMap = headers;
    }

    public void add(String headerName, String value) {
        final List<String> headerList = Optional.ofNullable(nameToValuesMap.get(headerName)).orElse(new LinkedList<>());
        headerList.add(value);
        put(headerName, headerList);
    }

    public void put(String headerName, List<String> values) {
        this.nameToValuesMap.put(headerName, values);
    }

    public List<String> getValues(String headerName) {
        return nameToValuesMap.get(headerName);
    }

    public Set<String> keySet() {
        return nameToValuesMap.keySet();
    }

    public void addAll(final HttpMultiValueMap other) {
        other.keySet().forEach(key -> {
            final List<String> values = other.getValues(key);
            if (values != null) {
                values.forEach(value -> this.add(key, value));
            } else {
                this.add(key, null);
            }
        });
    }
}
