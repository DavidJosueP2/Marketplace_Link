package com.gpis.marketplace_link.validation.annotation;

import com.gpis.marketplace_link.validation.validator.DniValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DniValidator.class)
@Documented
public @interface Cedula {
    String message() default "Cedula must be valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
