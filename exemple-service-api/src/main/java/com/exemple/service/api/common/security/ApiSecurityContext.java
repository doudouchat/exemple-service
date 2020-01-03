package com.exemple.service.api.common.security;

import java.security.Principal;
import java.util.Collection;
import java.util.function.Predicate;

import javax.ws.rs.core.SecurityContext;

import com.auth0.jwt.interfaces.Payload;

public class ApiSecurityContext implements SecurityContext {

    private final Principal principal;

    private final String scheme;

    private final Predicate<String> containsRole;

    private final Payload payload;

    private ApiSecurityContext(Principal principal, String scheme, Predicate<String> containsRole, Payload payload) {
        this.principal = principal;
        this.scheme = scheme;
        this.containsRole = containsRole;
        this.payload = payload;
    }

    public ApiSecurityContext(Principal principal, String scheme, Collection<String> roles, Payload payload) {
        this(principal, scheme, roles::contains, payload);
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

    public Payload getPayload() {
        return payload;
    }

}
