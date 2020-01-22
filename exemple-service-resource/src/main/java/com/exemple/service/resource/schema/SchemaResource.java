package com.exemple.service.resource.schema;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.resource.schema.model.SchemaEntity;

public interface SchemaResource {

    SchemaEntity get(@NotBlank String app, @NotBlank String version, @NotBlank String resource);

    Map<String, List<String>> allVersions(@NotBlank String app);

    void save(@NotNull SchemaEntity resourceSchema);

    void update(@NotNull SchemaEntity resourceSchema);

}
