package com.gpis.marketplace_link.validation.annotation;

import com.gpis.marketplace_link.validation.validator.ExistsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistsValidator.class)
public @interface Exists {

    String message() default "No existe este valor";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Entidad JPA a consultar, ej: Specialty.class */
    Class<?> entity();

    /** Nombre del campo dentro de la entidad. Por defecto 'id' usa em.find(...) */
    String field() default "id";

    /** Si false, permite null (útil cuando el campo es opcional) */
    boolean required() default true;

    /** Para campos String diferentes a id */
    boolean ignoreCase() default false;
}
