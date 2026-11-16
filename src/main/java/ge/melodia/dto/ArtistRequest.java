/*
 * ArtistRequest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Incoming data for creating or updating an artist.
 */
package ge.melodia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validated request body (or form object) for artist endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArtistRequest {

    @NotBlank(message = "Artist name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 2000, message = "Bio must be at most 2000 characters")
    private String bio;
}
