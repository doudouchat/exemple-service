package com.exemple.service.api.common.security;

import java.security.Principal;
import java.util.Collection;
import java.util.function.Predicate;

import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Getter;

public class ApiSecurityContext implements SecurityContext {

    private final Principal principal;

    private String scheme;

    private final Predicate<String> containsRole;

    @Getter
    private String profile;

    private ApiSecurityContext(Principal principal, String scheme, Predicate<String> containsRole, String profile) {
        this.principal = principal;
        this.scheme = scheme;
        this.containsRole = containsRole;
        this.profile = ObjectUtils.defaultIfNull(profile, ApiProfile.USER_PROFILE.profile);
    }

    public ApiSecurityContext(Principal principal, String scheme, Collection<String> roles, String profile) {
        this(principal, scheme, roles::contains, profile);
    }

    public ApiSecurityContext(Principal principal, String scheme) {
        this(principal, scheme, (String role) -> true, null);
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return containsRole.test(role);
    }

    @Override
    public boolean isSecure() {
        return "https".equals(this.scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }

}
