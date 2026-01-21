package com.exemple.service.application.common.model;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@Builder
@Getter
//TODO move to @Jacksonized
@JsonDeserialize(builder = ApplicationDetail.ApplicationDetailBuilder.class)
public class ApplicationDetail {

    @NotBlank
    private final String keyspace;

    @NotBlank
    private final String company;

    @NotEmpty
    @Singular
    private final Set<String> clientIds;

    @Builder.Default
    private final AccountDetail account = AccountDetail.builder().build();

    @JsonPOJOBuilder(withPrefix = "")
    public static class ApplicationDetailBuilder {
    }

    @Builder
    @Getter
  //TODO move to @Jacksonized
    @JsonDeserialize(builder = AccountDetail.AccountDetailBuilder.class)
    public static class AccountDetail {

        @NotEmpty
        @Singular
        private final Set<String> uniqueProperties;

        @JsonPOJOBuilder(withPrefix = "")
        public static class AccountDetailBuilder {
        }
    }

}
