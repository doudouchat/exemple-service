package com.exemple.service.api.core.actuate.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Info {

    private String version;

    private String buildTime;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

}
