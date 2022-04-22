package com.exemple.service.resource.schema;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;

public interface SchemaResource {

    Optional<SchemaEntity> get(@NotBlank String app, @NotBlank String version, @NotBlank String resource, @NotBlank String profile);

    Map<String, List<SchemaVersionProfileEntity>> allVersions(@NotBlank String app);

    void save(@NotNull SchemaEntity resourceSchema);

    void update(@NotNull SchemaEntity resourceSchema);

}
