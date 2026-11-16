/*
 * SongResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Song data returned to clients.
 */
package ge.melodia.dto;

import java.time.LocalDateTime;
import java.util.List;

import ge.melodia.entity.Album;
import ge.melodia.entity.Genre;
import ge.melodia.entity.Song;

/**
 * Read-only view of a song, including where it can be streamed from.
 *
 * @param id              song id
 * @param title           song title
 * @param durationSeconds length in seconds
 * @param playCount       how many times the song was played
 * @param albumId         id of the album
 * @param albumTitle      title of the album
 * @param coverUrl        album cover URL, or null
 * @param artistId        id of the artist
 * @param artistName      name of the artist
 * @param genres          genre names
 * @param streamUrl       URL that streams the audio file
 * @param createdAt       upload time
 */
public record SongResponse(Long id, String title, int durationSeconds,
        long playCount, Long albumId, String albumTitle, String coverUrl,
        Long artistId, String artistName, List<String> genres,
        String streamUrl, LocalDateTime createdAt) {

    public static SongResponse from(Song song) {
        Album album = song.getAlbum();
        List<String> genreNames = song.getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();

        return new SongResponse(song.getId(), song.getTitle(),
                song.getDurationSeconds(), song.getPlayCount(),
                album.getId(), album.getTitle(),
                MediaUrls.image(album.getCoverFileName()),
                album.getArtist().getId(), album.getArtist().getName(),
                genreNames, MediaUrls.stream(song.getId()),
                song.getCreatedAt());
    }

    /**
     * Formats the duration as m:ss, for example 3:07.
     *
     * @return formatted duration
     */
    public String formattedDuration() {
        return MediaUrls.formatDuration(durationSeconds);
    }
}
