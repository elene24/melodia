/*
 * SongRequest.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Incoming song metadata (the audio file is sent separately).
 */
package ge.melodia.dto;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Song metadata. Bound with {@code @ModelAttribute} from a
 * multipart/form-data upload, or with {@code @RequestBody} on update.
 */
@Getter
@Setter
@NoArgsConstructor
public class SongRequest {

    @NotBlank(message = "Song title is required")
    @Size(max = 150, message = "Title must be at most 150 characters")
    private String title;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 second")
    @Max(value = 3600, message = "Duration must be at most 3600 seconds")
    private Integer durationSeconds;

    @NotNull(message = "Album is required")
    private Long albumId;

    @Size(max = 5, message = "A song can have at most 5 genres")
    private Set<Long> genreIds = new HashSet<>();
}
