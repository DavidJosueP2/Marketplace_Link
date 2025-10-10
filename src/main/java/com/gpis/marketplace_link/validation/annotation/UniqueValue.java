package com.gpis.marketplace_link.validation.annotation;

import com.gpis.marketplace_link.validation.validator.UniqueValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValueValidator.class)
public @interface UniqueValue {

    String message() default "Ya está registrado.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Entidad JPA a consultar, ej: Specialty.class */
    Class<?> entity();

    /** Nombre del campo dentro de la entidad JPA, ej: "name" */
    String field();

    /** Ignora mayúsculas/minúsculas en la comparación */
    boolean ignoreCase() default true;

    /** Nombre del path variable donde viene el id en el UPDATE (default: "id") */
    String pathVariable() default "id";

    /** Nombre del campo id en la entidad (default: "id") */
    String idField() default "id";
}
