package antifraud.model;

import antifraud.service.ValidationResult;
import antifraud.validation.EnumConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionFeedbackInDto {
    @Positive
    private Long transactionId;
    @NotNull
    @EnumConstraint(enumClass = ValidationResult.class)
    private String feedback;
}
