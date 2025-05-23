package com.exemple.service.api.stock.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Jacksonized
@Getter
public class Stock {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Long amount;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final String product;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final String store;

}
