package com.exemple.service.resource.account.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Child {

    private final Object birthday;

    public Object getBirthday() {
        return birthday;
    }

    public Child(Object birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("birthday", birthday).toString();
    }
}
