package com.exemple.service.resource.core.keystore;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;

@Component
public class ResourceKeystore {

    private final ApplicationDetailService applicationDetailService;

    public ResourceKeystore(ApplicationDetailService applicationDetailService) {
        this.applicationDetailService = applicationDetailService;
    }

    public void initKeyspaceResourceContext(String app) {

        if (ResourceExecutionContext.get().isKeyspaceNull()) {

            Assert.notNull(app, "App is required");

            ApplicationDetail applicationDetail = applicationDetailService.get(app);
            ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());

        }

    }

}
