package antifraud.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD,
        ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumConstraint {
    String message() default "Value is not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
