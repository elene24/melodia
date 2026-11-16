/*
 * PlaylistResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Full playlist data including its songs.
 */
package ge.melodia.dto;

import java.time.LocalDateTime;
import java.util.List;

import ge.melodia.entity.Playlist;

/**
 * Playlist with songs and total duration.
 *
 * @param id                   playlist id
 * @param name                 playlist name
 * @param description          description
 * @param ownerId              id of the owner
 * @param ownerName            display name of the owner
 * @param totalDurationSeconds sum of the song durations
 * @param songs                songs in the playlist
 * @param createdAt            creation time
 * @param updatedAt            last modification time
 */
public record PlaylistResponse(Long id, String name, String description,
        Long ownerId, String ownerName, long totalDurationSeconds,
        List<SongResponse> songs, LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PlaylistResponse from(Playlist playlist,
            long totalDurationSeconds) {
        List<SongResponse> songs = playlist.getSongs().stream()
                .map(SongResponse::from)
                .toList();

        return new PlaylistResponse(playlist.getId(), playlist.getName(),
                playlist.getDescription(), playlist.getOwner().getId(),
                playlist.getOwner().getDisplayName(), totalDurationSeconds,
                songs, playlist.getCreatedAt(), playlist.getUpdatedAt());
    }

    /**
     * Formats the total duration, for example 1 h 4 min or 12 min 5 s.
     *
     * @return formatted total duration
     */
    public String formattedDuration() {
        long hours = totalDurationSeconds / 3600;
        long minutes = (totalDurationSeconds % 3600) / 60;
        long seconds = totalDurationSeconds % 60;

        if (hours > 0) {
            return hours + " h " + minutes + " min";
        }
        return minutes + " min " + seconds + " s";
    }
}
