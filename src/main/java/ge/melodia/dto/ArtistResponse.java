/*
 * ArtistResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Artist data returned to clients.
 */
package ge.melodia.dto;

import ge.melodia.entity.Artist;

/**
 * Read-only view of an artist.
 *
 * @param id         artist id
 * @param name       artist name
 * @param bio        short biography
 * @param imageUrl   public URL of the photo, or null
 * @param albumCount number of albums
 */
public record ArtistResponse(Long id, String name, String bio,
        String imageUrl, int albumCount) {

    public static ArtistResponse from(Artist artist) {
        return new ArtistResponse(artist.getId(), artist.getName(),
                artist.getBio(), MediaUrls.image(artist.getImageFileName()),
                artist.getAlbums().size());
    }
}
