package com.exemple.service.customer.core.validation;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.TransformUtils;
import com.exemple.service.customer.common.ValidationCustom;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class ValidationCustomAspect {

    private final ValidationCustom validationCustom;

    public ValidationCustomAspect(ValidationCustom validationCustom) {
        this.validationCustom = validationCustom;
    }

    @After("execution(public void com.exemple.service.schema.validation.SchemaValidation.validate(.., *, *)) && args(.., resource, form)")
    public void validate(String resource, JsonNode form) {

        ValidationCustomContext.context().setResource(resource);

        TransformUtils.accept(form, validationCustom::validate);

        ValidationCustomContext.destroy();

    }

    @After("execution(public void com.exemple.service.schema.validation.SchemaValidation.validate(.., *, *, *)) && args(.., resource, form, old)")
    public void validate(String resource, JsonNode form, JsonNode old) {

        ValidationCustomContext.context().setResource(resource);

        TransformUtils.accept(form, old, validationCustom::validate);

        ValidationCustomContext.destroy();

    }

}
