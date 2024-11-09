package antifraud.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<EnumConstraint, String> {
    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumConstraint constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || enumClass == null) {
            return true; // null values are considered valid, mark them as @NotNull if needed
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(e -> e.name().equals(value));
    }
}