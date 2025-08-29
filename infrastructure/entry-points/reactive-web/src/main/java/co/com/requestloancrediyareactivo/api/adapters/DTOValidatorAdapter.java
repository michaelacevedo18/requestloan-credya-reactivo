package co.com.requestloancrediyareactivo.api.adapters;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.springframework.stereotype.Component;

import java.util.Set;


@Component
public class DTOValidatorAdapter {
    private final Validator validator;

    public DTOValidatorAdapter(Validator validator) {
        this.validator = validator;
    }

    public <T> void validateOrThrow(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
