package com.exemple.service.resource.account.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Account {

    private Object email;

    private Object lastname;

    private Object firstname;

    private Object birthday;

    private Object age;

    private Object subscription_1;

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

    private Object content;

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

    public Object getSubscription_1() {
        return subscription_1;
    }

    public void setSubscription_1(Object subscription_1) {
        this.subscription_1 = subscription_1;
    }

    public Map<String, Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, Address> addresses) {
        this.addresses = addresses;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lastname", lastname).append("firstname", firstname).append("birthday", birthday).append("age", age)
                .append("subscription_1", subscription_1).append("addresses", addresses).toString();
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

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

}
