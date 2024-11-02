package antifraud.model;


import antifraud.validation.ValidCardNumber;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionDTO(
        @NotNull
        @Positive
        Long amount,
        @NotEmpty
        String ip,
        @NotEmpty
        @ValidCardNumber
        String number) {
}
