package com.exemple.service.customer.common.script;

import java.util.Map;

public interface CustomiseValidation {

    default void validate(Map<String, Object> form) {

    }

    default void validate(Map<String, Object> form, Map<String, Object> old) {

    }

}
