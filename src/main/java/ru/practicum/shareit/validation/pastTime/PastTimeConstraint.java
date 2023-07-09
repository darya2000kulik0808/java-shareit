package ru.practicum.shareit.validation.pastTime;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PastTimeConstraintValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PastTimeConstraint {
    String message() default "Неправильная дата начала или окончания бронирования. Нельзя ставить дату," +
            " предшествующую текущей.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
