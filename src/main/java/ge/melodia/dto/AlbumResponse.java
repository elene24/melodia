/*
 * AlbumResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Album data returned to clients.
 */
package ge.melodia.dto;

import java.time.LocalDate;

import ge.melodia.entity.Album;

/**
 * Read-only view of an album.
 *
 * @param id          album id
 * @param title       album title
 * @param releaseDate release date
 * @param coverUrl    public URL of the cover, or null
 * @param artistId    id of the artist
 * @param artistName  name of the artist
 * @param songCount   number of songs
 */
public record AlbumResponse(Long id, String title, LocalDate releaseDate,
        String coverUrl, Long artistId, String artistName, int songCount) {

    public static AlbumResponse from(Album album) {
        return new AlbumResponse(album.getId(), album.getTitle(),
                album.getReleaseDate(),
                MediaUrls.image(album.getCoverFileName()),
                album.getArtist().getId(), album.getArtist().getName(),
                album.getSongs().size());
    }
}
