package com.exemple.service.resource.schema;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.resource.schema.model.ResourceSchema;

public interface SchemaResource {

    byte[] get(@NotBlank String app, @NotBlank String version, @NotBlank String resource);

    Map<String, List<String>> allVersions(@NotBlank String app);

    Set<String> getFilter(@NotBlank String app, @NotBlank String version, @NotBlank String resource);

    Map<String, Set<String>> getRule(@NotBlank String app, @NotBlank String version, @NotBlank String resource);

    void save(@NotNull ResourceSchema resourceSchema);

    void update(@NotNull ResourceSchema resourceSchema);

}
