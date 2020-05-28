package com.exemple.service.context;

import java.security.Principal;
import java.time.OffsetDateTime;

public class ServiceContext {

    private OffsetDateTime date = OffsetDateTime.now();

    private Principal principal = () -> "anonymous";

    private String app;

    private String version;

    private String profile;

    public String getApp() {
        return app;
    }

    public String getVersion() {
        return version;
    }

    public String getProfile() {
        return profile;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

}
