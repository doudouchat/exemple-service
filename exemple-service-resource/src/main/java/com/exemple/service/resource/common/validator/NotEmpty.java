package com.exemple.service.resource.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;

@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NotEmptyConstraintValidator.class)
@ReportAsSingleViolation
@NotNull
public @interface NotEmpty {

    String message() default "{javax.validation.constraints.NotEmpty.jsonNode.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
