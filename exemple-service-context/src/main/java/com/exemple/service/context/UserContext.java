package com.exemple.service.context;

import java.security.Principal;

public record UserContext(Principal principal) {

    public static final ScopedValue<UserContext> USER_CONTEXT = ScopedValue.newInstance();

}
