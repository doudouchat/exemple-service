package com.exemple.service.resource.schema.model;

import java.io.Serializable;

public class SchemaVersionProfileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String version;

    private String profile;

    public SchemaVersionProfileEntity(String version, String profile) {
        this.version = version;
        this.profile = profile;
    }

    public String getVersion() {
        return version;
    }

    public String getProfile() {
        return profile;
    }

}
