/*
 * PlaylistSummaryResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Short playlist data used in lists.
 */
package ge.melodia.dto;

import java.time.LocalDateTime;

import ge.melodia.entity.Playlist;

/**
 * Playlist without its songs.
 *
 * @param id          playlist id
 * @param name        playlist name
 * @param description description
 * @param ownerId     id of the owner
 * @param ownerName   display name of the owner
 * @param songCount   number of songs
 * @param updatedAt   last modification time
 */
public record PlaylistSummaryResponse(Long id, String name,
        String description, Long ownerId, String ownerName, int songCount,
        LocalDateTime updatedAt) {

    public static PlaylistSummaryResponse from(Playlist playlist) {
        return new PlaylistSummaryResponse(playlist.getId(),
                playlist.getName(), playlist.getDescription(),
                playlist.getOwner().getId(),
                playlist.getOwner().getDisplayName(),
                playlist.getSongs().size(), playlist.getUpdatedAt());
    }
}
