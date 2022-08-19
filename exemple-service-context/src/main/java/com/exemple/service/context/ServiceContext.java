package com.exemple.service.context;

import java.security.Principal;
import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class ServiceContext {

    private final OffsetDateTime date;

    private final Principal principal;

    private final String app;

    private final String version;

}
