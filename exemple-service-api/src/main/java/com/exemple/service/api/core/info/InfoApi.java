package com.exemple.service.api.core.info;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;
import org.springframework.stereotype.Component;

import com.exemple.service.api.core.ApiContext;
import com.exemple.service.api.core.info.model.Info;

import lombok.RequiredArgsConstructor;

@Path("/")
@Component
@RequiredArgsConstructor
public class InfoApi {

    private final ApiContext apiContext;

    @GET
    @Template(name = "/info")
    @Produces({ MediaType.TEXT_XML, MediaType.TEXT_HTML })
    public Info template() {

        return Info.builder().version(apiContext.getVersion()).buildTime(apiContext.getBuildTime()).build();
    }
}
