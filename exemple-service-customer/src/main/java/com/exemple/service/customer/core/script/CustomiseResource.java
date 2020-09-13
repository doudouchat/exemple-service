package com.exemple.service.customer.core.script;

import java.util.Map;

public interface CustomiseResource {

    Map<String, Object> create(Map<String, Object> source);

    default Map<String, Object> update(Map<String, Object> source, Map<String, Object> previousSource) {
        return source;
    }

}
