/*
 * SongStatsResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Like statistics of a song (filled by a JPQL constructor expression).
 */
package ge.melodia.dto;

/**
 * Number of likes of a song.
 *
 * @param songId     song id
 * @param title      song title
 * @param artistName artist name
 * @param likes      number of users who liked the song
 */
public record SongStatsResponse(Long songId, String title,
        String artistName, Long likes) {
}
