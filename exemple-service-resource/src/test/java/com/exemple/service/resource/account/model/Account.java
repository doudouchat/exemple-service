package com.exemple.service.resource.account.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
public class Account {

    private final UUID id;

    private final Object email;

    private final Object birthday;

    private final Object age;

    private final Object enabled;

    private final Object address;

    @Singular(ignoreNullCollections = true)
    private final Map<String, Address> addresses;

    @Singular(ignoreNullCollections = true)
    private final Map<String, Child> children;

    @Singular(value = "cgu", ignoreNullCollections = true)
    private final Set<Cgu> cgus;

    private final Object status;

    private final Object creation_date;

    @Singular(ignoreNullCollections = true)
    private final Set<Object> profils;

    @Singular(ignoreNullCollections = true)
    private final Map<Object, Object> phones;

    @Singular(ignoreNullCollections = true)
    private final Map<Object, Object> notes;

    @Singular(ignoreNullCollections = true)
    private final List<Object> preferences;

}
