package com.exemple.service.customer.account.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Account {

    private Object email;

    private Object lastname;

    private Object firstname;

    private Object civility;

    @JsonProperty("opt_in_email")
    private Object optinEmail;

    public Object getEmail() {
        return email;
    }

    public void setEmail(Object email) {
        this.email = email;
    }

    public Object getLastname() {
        return lastname;
    }

    public void setLastname(Object lastname) {
        this.lastname = lastname;
    }

    public Object getFirstname() {
        return firstname;
    }

    public void setFirstname(Object firstname) {
        this.firstname = firstname;
    }

    public Object getOptinEmail() {
        return optinEmail;
    }

    public void setOptinEmail(Object optinEmail) {
        this.optinEmail = optinEmail;
    }

    public Object getCivility() {
        return civility;
    }

    public void setCivility(Object civility) {
        this.civility = civility;
    }

}
