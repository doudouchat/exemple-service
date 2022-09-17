package com.exemple.service.api.core.listener;

import javax.annotation.PostConstruct;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.slf4j.bridge.SLF4JBridgeHandler;

import lombok.extern.slf4j.Slf4j;

@Provider
@Slf4j
public class ApiEventListener implements ApplicationEventListener {

    @PostConstruct
    public void initLog() {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.getType()) {

            case INITIALIZATION_START:
                LOG.info("Démarrage de l'application {}", event.getResourceConfig().getApplicationName());
                break;
            case DESTROY_FINISHED:
                LOG.info("Arrêt de l'application {}", event.getResourceConfig().getApplicationName());
                break;
            default:
                break;
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {

        return (RequestEvent event) -> {
            if (RequestEvent.Type.REQUEST_MATCHED == event.getType()) {
                LOG.debug("Appel de la méthode {} {}", event.getUriInfo().getMatchedResourceMethod().getHttpMethod(),
                        event.getUriInfo().getAbsolutePath());

            }
        };
    }

}
