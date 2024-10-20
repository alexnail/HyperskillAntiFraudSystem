package antifraud.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UserAccessDTO(
        @NotEmpty
        String username,
        @Pattern(regexp = "LOCK|UNLOCK", message = "Operation must be either 'LOCK' or 'UNLOCK'")
        String operation
) {
}
