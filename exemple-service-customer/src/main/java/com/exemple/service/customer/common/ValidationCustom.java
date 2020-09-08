package com.exemple.service.customer.common;

import java.util.Map;

public interface ValidationCustom {

    void validate(Map<String, Object> form);

    void validate(Map<String, Object> form, Map<String, Object> old);

}
