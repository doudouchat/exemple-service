package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.exemple.service.api.common.security.ApiSecurityContext;

public interface AuthorizationContextService {

    ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException;

    void cleanContext(ApiSecurityContext securityContext, Response.StatusType statusInfo);

}
