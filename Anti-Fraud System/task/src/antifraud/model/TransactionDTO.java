package antifraud.model;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionDTO(
        @NotNull
        @Positive
        Long amount) {
}
