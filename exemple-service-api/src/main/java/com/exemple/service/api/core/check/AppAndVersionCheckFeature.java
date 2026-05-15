package com.exemple.service.api.core.check;

import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.springframework.context.MessageSource;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AppAndVersionCheckFeature implements DynamicFeature {

    private final MessageSource messageSource;

    @Inject
    public AppAndVersionCheckFeature(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        var am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        AppAndVersionCheck appAndVersion = am.getAnnotation(AppAndVersionCheck.class);
        if (appAndVersion != null) {
            context.register(new AppAndVersionCheckRequestFilter(appAndVersion.optionalVersion(), messageSource));
        }

    }

}
