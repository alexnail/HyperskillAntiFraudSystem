package antifraud.model;


import antifraud.validation.ValidCardNumber;
import antifraud.validation.ValidRegion;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record TransactionDTO(
        @NotNull
        @Positive
        Long amount,
        @NotEmpty
        String ip,
        @NotEmpty
        @ValidCardNumber
        String number,
        @NotEmpty
        @ValidRegion
        String region,
        LocalDateTime date) {
}
