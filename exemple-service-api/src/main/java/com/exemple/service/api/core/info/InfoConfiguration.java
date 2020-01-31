package com.exemple.service.api.core.info;

import java.util.logging.Level;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

@ApplicationPath("/info")
public class InfoConfiguration extends ResourceConfig {

    public InfoConfiguration() {

        // Resources
        packages(
                // info
                "com.exemple.service.api.core.info")

                        // Nom de l'application
                        .setApplicationName("JS Service")

                        // logging

                        .register(LoggingFeature.class)

                        .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY)

                        .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, Level.FINE.getName())

                        // JSP

                        .register(JspMvcFeature.class)

                        .property(JspMvcFeature.TEMPLATE_BASE_PATH, "/WEB-INF/pages");

    }

}
