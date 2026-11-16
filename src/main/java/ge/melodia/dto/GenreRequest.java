/*
 * GenreRequest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Incoming data for creating a genre.
 */
package ge.melodia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validated request body for genre endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class GenreRequest {

    @NotBlank(message = "Genre name is required")
    @Size(max = 50, message = "Name must be at most 50 characters")
    private String name;
}
