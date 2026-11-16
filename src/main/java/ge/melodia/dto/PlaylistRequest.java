/*
 * PlaylistRequest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Incoming data for creating or updating a playlist.
 */
package ge.melodia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validated request body (or form object) for playlist endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class PlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}
