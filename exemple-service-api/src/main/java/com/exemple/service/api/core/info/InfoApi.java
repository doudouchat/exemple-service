package com.exemple.service.api.core.info;

import org.glassfish.jersey.server.mvc.Template;
import org.springframework.stereotype.Component;

import com.exemple.service.api.core.ApiContext;
import com.exemple.service.api.core.info.model.Info;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
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
