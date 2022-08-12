package com.exemple.service.api.core.check;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

@Provider
public class AppAndVersionCheckFeature implements DynamicFeature {

    @Autowired
    private MessageSource messageSource;

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        var am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        AppAndVersionCheck appAndVersion = am.getAnnotation(AppAndVersionCheck.class);
        if (appAndVersion != null) {
            context.register(new AppAndVersionCheckRequestFilter(appAndVersion.optionalVersion(), messageSource));
        }

    }

}
