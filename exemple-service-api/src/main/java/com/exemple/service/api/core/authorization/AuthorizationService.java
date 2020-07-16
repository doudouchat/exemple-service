package com.exemple.service.api.core.authorization;

import javax.ws.rs.core.Response;

public interface AuthorizationService {

    Response tokenKey(String path, String username, String password);

}
