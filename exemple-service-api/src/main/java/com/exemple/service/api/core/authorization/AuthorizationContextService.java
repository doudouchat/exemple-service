package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.MultivaluedMap;

import com.exemple.service.api.common.security.ApiSecurityContext;

public interface AuthorizationContextService {

    ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException;

}
