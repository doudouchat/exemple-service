package com.exemple.service.api.common.security;

public enum ApiProfile {

    USER_PROFILE("user");

    public final String profile;

    ApiProfile(String profile) {
        this.profile = profile;
    }

}
