package com.exemple.service.resource.account.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Account {

    private Object email;

    private Object birthday;

    private Object age;

    private Object enabled;

    private Object address;

    private Map<String, Address> addresses;

    private Map<String, Child> children;

    private Set<Cgu> cgus;

    private Object status;

    private Object creation_date;

    private Set<Object> profils;

    private Map<Object, Object> phones;

    private Map<Object, Object> notes;

    private List<Object> preferences;

    public Object getBirthday() {
        return birthday;
    }

    public void setBirthday(Object birthday) {
        this.birthday = birthday;
    }

    public Object getAge() {
        return age;
    }

    public void setAge(Object age) {
        this.age = age;
    }

    public Object getEnabled() {
        return enabled;
    }

    public void setEnabled(Object enabled) {
        this.enabled = enabled;
    }

    public Map<String, Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, Address> addresses) {
        this.addresses = addresses;
    }

    public Object getEmail() {
        return email;
    }

    public void setEmail(Object email) {
        this.email = email;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Map<String, Child> getChildren() {
        return children;
    }

    public void setChildren(Map<String, Child> children) {
        this.children = children;
    }

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public Set<Cgu> getCgus() {
        return cgus;
    }

    public void setCgus(Set<Cgu> cgus) {
        this.cgus = cgus;
    }

    public Object getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Object creation_date) {
        this.creation_date = creation_date;
    }

    public Set<Object> getProfils() {
        return profils;
    }

    public void setProfils(Set<Object> profils) {
        this.profils = profils;
    }

    public Map<Object, Object> getPhones() {
        return phones;
    }

    public void setPhones(Map<Object, Object> phones) {
        this.phones = phones;
    }

    public Map<Object, Object> getNotes() {
        return notes;
    }

    public void setNotes(Map<Object, Object> notes) {
        this.notes = notes;
    }

    public List<Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Object> preferences) {
        this.preferences = preferences;
    }

}
