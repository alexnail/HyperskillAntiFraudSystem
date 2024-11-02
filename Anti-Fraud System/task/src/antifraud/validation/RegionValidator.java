package antifraud.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class RegionValidator implements ConstraintValidator<ValidRegion, String> {

    private static Set<String> VALID_REGIONS = Set.of("EAP", "ECA", "HIC", "LAC", "MENA", "SA", "SSA");

    @Override
    public boolean isValid(String region, ConstraintValidatorContext constraintValidatorContext) {
        return VALID_REGIONS.contains(region);
    }
}
