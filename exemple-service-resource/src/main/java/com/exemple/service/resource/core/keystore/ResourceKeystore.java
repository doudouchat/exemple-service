package com.exemple.service.resource.core.keystore;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResourceKeystore {

    private final ApplicationDetailService applicationDetailService;

    public void initKeyspaceResourceContext(String app) {

        if (ResourceExecutionContext.get().isKeyspaceNull()) {

            Assert.notNull(app, "App is required");

            var applicationDetail = applicationDetailService.get(app).orElseThrow(() -> new NotFoundApplicationException(app));
            ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());

        }

    }

}
