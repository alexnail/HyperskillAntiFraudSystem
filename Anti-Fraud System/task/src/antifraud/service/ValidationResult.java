package antifraud.service;

import java.util.Set;

public enum ValidationResult {
    ALLOWED, MANUAL_PROCESSING, PROHIBITED;

    public static ValidationResult getStrictest(Set<ValidationResult> set) {
        return set.stream().max(ValidationResult::compareTo).orElse(PROHIBITED);
    }
}
