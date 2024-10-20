package antifraud.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UserRoleDTO(
        @NotEmpty
        String username,
        @NotEmpty
        @Pattern(regexp = "SUPPORT|MERCHANT", message = "Only 'SUPPORT' or 'MERCHANT' roles can be set using the API")
        String role) {
}
