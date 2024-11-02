package antifraud.model;

import antifraud.service.ValidationResult;

public record ValidationResultDTO(
        ValidationResult result,
        String info) {
}
