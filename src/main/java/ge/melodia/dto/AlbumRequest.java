/*
 * AlbumRequest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Incoming data for creating or updating an album.
 */
package ge.melodia.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Validated request body (or form object) for album endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class AlbumRequest {

    @NotBlank(message = "Album title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @NotNull(message = "Release date is required")
    @PastOrPresent(message = "Release date cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate;

    @NotNull(message = "Artist is required")
    @Positive(message = "Artist id must be positive")
    private Long artistId;
}
