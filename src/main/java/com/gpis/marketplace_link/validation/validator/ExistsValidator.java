package com.gpis.marketplace_link.validation.validator;

import com.gpis.marketplace_link.validation.annotation.Exists;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExistsValidator implements ConstraintValidator<Exists, Object> {

    @PersistenceContext
    private EntityManager em;

    private Class<?> entityClass;
    private String field;
    private boolean required;
    private boolean ignoreCase;

    @Override
    public void initialize(Exists ann) {
        this.entityClass = ann.entity();
        this.field = ann.field();
        this.required = ann.required();
        this.ignoreCase = ann.ignoreCase();
        if (entityClass == null) {
            throw new IllegalArgumentException("@Exists: 'entity' es requerido");
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null) return !required;

        if ("id".equals(field)) {
            Object found = em.find(entityClass, value);
            return found != null;
        }

        StringBuilder jpql = new StringBuilder("select 1 from ")
                .append(entityClass.getName()).append(" e where ");
        if (ignoreCase && value instanceof String) {
            jpql.append("lower(e.").append(field).append(") = lower(:val)");
        } else {
            jpql.append("e.").append(field).append(" = :val");
        }

        List<?> result = em.createQuery(jpql.toString())
                .setParameter("val", value)
                .setMaxResults(1)
                .getResultList();

        return !result.isEmpty();
    }
}
