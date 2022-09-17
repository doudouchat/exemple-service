package com.exemple.service.api.core.feature;

import java.util.logging.Level;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.validation.ValidationConfig;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import com.exemple.service.api.common.schema.SchemaFilter;
import com.exemple.service.api.common.schema.SchemaFilterSupplier;
import com.exemple.service.api.common.schema.SchemaValidation;
import com.exemple.service.api.common.schema.SchemaValidationSupplier;
import com.exemple.service.api.common.script.AccountServiceSupplier;
import com.exemple.service.api.common.script.SubscriptionServiceSupplier;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.authorization.AuthorizationCheckServiceSupplier;
import com.exemple.service.api.core.authorization.AuthorizationFilter;
import com.exemple.service.api.core.check.AppAndVersionCheckFeature;
import com.exemple.service.api.core.filter.ExecutionContextResponseFilter;
import com.exemple.service.api.core.listener.ApiEventListener;
import com.exemple.service.api.core.swagger.DocumentApiResource;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ApplicationPath("/ws")
public class FeatureConfiguration extends ResourceConfig {

    public FeatureConfiguration() {

        // Resources
        packages(
                // exception provider
                "com.exemple.service.api.core.exception",
                // account
                "com.exemple.service.api.account",
                // connexion
                "com.exemple.service.api.connexion",
                // schema
                "com.exemple.service.api.schema",
                // login
                "com.exemple.service.api.login",
                // subscription
                "com.exemple.service.api.subscription",
                // stock
                "com.exemple.service.api.stock")
                // Nom de l'application
                .setApplicationName("WS Service")

                // validation
                .register(ValidationConfigurationContextResolver.class)

                // swagger

                .register(DocumentApiResource.class)

                // execution context

                .register(ExecutionContextResponseFilter.class)

                // listener event

                .register(ApiEventListener.class)

                // security

                .register(RolesAllowedDynamicFeature.class)

                .register(AuthorizationFilter.class)

                // check
                .register(AppAndVersionCheckFeature.class)

                // logging

                .register(LoggingFeature.class)

                .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY)

                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, Level.FINE.getName())

                // JSON
                .register(JacksonJsonProvider.class)

                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bindFactory(SchemaValidationSupplier.class)
                                .to(SchemaValidation.class)
                                .in(RequestScoped.class);

                        bindFactory(SchemaFilterSupplier.class)
                                .to(SchemaFilter.class)
                                .in(RequestScoped.class);

                        bindFactory(AuthorizationCheckServiceSupplier.class)
                                .to(AuthorizationCheckService.class)
                                .in(RequestScoped.class);

                        bindFactory(AccountServiceSupplier.class)
                                .to(AccountService.class)
                                .in(RequestScoped.class);

                        bindFactory(SubscriptionServiceSupplier.class)
                                .to(SubscriptionService.class)
                                .in(RequestScoped.class);
                    }
                });

    }

    protected static class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

        @Override
        public ValidationConfig getContext(Class<?> type) {

            var config = new ValidationConfig();

            var messageSource = new ReloadableResourceBundleMessageSource();

            messageSource.setCacheSeconds(0);
            messageSource.setBasename("classpath:messages/erreur_messages");

            config.messageInterpolator(new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(messageSource)));

            return config;
        }

    }

}
