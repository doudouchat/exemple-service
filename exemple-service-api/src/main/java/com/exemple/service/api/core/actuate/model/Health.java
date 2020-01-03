package com.exemple.service.api.core.actuate.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Health {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
