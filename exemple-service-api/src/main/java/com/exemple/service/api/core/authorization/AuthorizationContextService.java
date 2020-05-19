package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ObjectUtils;

import com.exemple.service.api.common.security.ApiProfile;
import com.exemple.service.api.common.security.ApiSecurityContext;

public interface AuthorizationContextService {

    ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException;

    void cleanContext(ApiSecurityContext securityContext, Response.StatusType statusInfo);

    default String getProfile(ApiSecurityContext securityContext, String defaultProfile) {

        return ObjectUtils.defaultIfNull(securityContext.getProfile(), defaultProfile);
    }

    default String getUserProfile(ApiSecurityContext securityContext) {

        return getProfile(securityContext, ApiProfile.USER_PROFILE.profile);
    }

}
