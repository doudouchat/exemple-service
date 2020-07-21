package com.exemple.service.resource.account.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Address {

    private final Object street;

    private final Object city;

    private final Object zip;

    private final Object floor;

    private final Object enable = null;

    public Address(Object street, Object city) {
        this(street, city, null, null);
    }

    public Address(Object street, Object city, Object zip, Object floor) {
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.floor = floor;
    }

    public Object getStreet() {
        return street;
    }

    public Object getCity() {
        return city;
    }

    public Object getZip() {
        return zip;
    }

    public Object getFloor() {
        return floor;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("street", street).append("city", city).append("zip", zip).append("floor", floor).toString();
    }

    public Object getEnable() {
        return enable;
    }
}
