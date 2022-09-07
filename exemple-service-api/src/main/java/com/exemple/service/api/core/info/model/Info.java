package com.exemple.service.api.core.info.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Builder;
import lombok.Getter;

@XmlRootElement
@Builder
@Getter
public class Info {

    private final String version;

    private final String buildTime;

}
