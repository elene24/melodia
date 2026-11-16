/*
 * UserRequest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Incoming data for creating or updating a user.
 */
package ge.melodia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validated request body for user endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30,
            message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$",
            message = "Username may contain letters, digits, '_' and '.'")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;

    @NotBlank(message = "Display name is required")
    @Size(max = 60, message = "Display name must be at most 60 characters")
    private String displayName;
}
