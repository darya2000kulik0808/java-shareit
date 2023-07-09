package ru.practicum.shareit.validation.pastTime;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class PastTimeConstraintValidator implements
        ConstraintValidator<PastTimeConstraint, LocalDate> {
    @Override
    public void initialize(PastTimeConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        return localDate.isAfter(LocalDate.now());
    }
}
