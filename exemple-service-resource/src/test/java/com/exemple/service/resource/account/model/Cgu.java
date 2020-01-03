package com.exemple.service.resource.account.model;

public class Cgu {

    private final Object code;

    private final Object version;

    public Cgu(Object code, Object version) {
        this.code = code;
        this.version = version;
    }

    public Object getCode() {
        return code;
    }

    public Object getVersion() {
        return version;
    }

}
