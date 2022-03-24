package com.exemple.service.resource.account.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Address {

    private final Object street;

    private final Object city;

    private final Object zip;

    private final Object floor;

    private final Object enable;
}
