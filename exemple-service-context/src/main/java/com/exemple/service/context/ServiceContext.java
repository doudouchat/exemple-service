package com.exemple.service.context;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public record ServiceContext(OffsetDateTime date,
                             String app,
                             String version) {

    public static final ScopedValue<ServiceContext> SERVICE_CONTEXT = ScopedValue.newInstance();

    public ServiceContext(String app, String version) {
        this(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS), app, version);
    }

}
