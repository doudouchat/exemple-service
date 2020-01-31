package com.exemple.service.api.core.info;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.core.ApiContext;
import com.exemple.service.api.core.info.model.Info;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/")
@OpenAPIDefinition(tags = @Tag(name = "info"))
@Component
public class InfoApi {

    @Autowired
    private ApiContext apiContext;

    @GET
    @Template(name = "/info")
    @Produces({ MediaType.TEXT_XML, MediaType.TEXT_HTML })
    public Info template() {

        Info result = new Info();
        result.setVersion(apiContext.getVersion());
        result.setBuildTime(apiContext.getBuildTime());

        return result;
    }
}
