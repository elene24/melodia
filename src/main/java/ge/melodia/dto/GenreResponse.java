/*
 * GenreResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Genre data returned to clients.
 */
package ge.melodia.dto;

import ge.melodia.entity.Genre;

/**
 * Read-only view of a genre.
 *
 * @param id   genre id
 * @param name genre name
 */
public record GenreResponse(Long id, String name) {

    public static GenreResponse from(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName());
    }
}
