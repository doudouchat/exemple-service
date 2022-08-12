package com.exemple.service.resource.schema.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SchemaVersionProfileEntity implements Serializable {

    private final String version;

    private final String profile;
}
