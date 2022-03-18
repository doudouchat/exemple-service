package com.exemple.service.context;

import java.security.Principal;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceContext {

    private OffsetDateTime date = OffsetDateTime.now();

    private Principal principal = () -> "anonymous";

    private String app;

    private String version;

}
