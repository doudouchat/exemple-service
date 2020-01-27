package com.exemple.service.resource.parameter.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Entity
@CqlName("parameter")
public class ParameterEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @PartitionKey
    @CqlName("app")
    private String application;

    private Map<String, Boolean> histories = Collections.emptyMap();

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Map<String, Boolean> getHistories() {
        return histories;
    }

    public void setHistories(Map<String, Boolean> histories) {
        this.histories = histories;
    }

}
