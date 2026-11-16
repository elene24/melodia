/*
 * UserResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * User data returned to clients.
 */
package ge.melodia.dto;

import java.time.LocalDateTime;

import ge.melodia.entity.User;

/**
 * Read-only view of a user.
 *
 * @param id                  user id
 * @param username            unique username
 * @param email               email address
 * @param displayName         display name
 * @param playlistCount       number of playlists
 * @param likedSongCount      number of liked songs
 * @param followedArtistCount number of followed artists
 * @param createdAt           registration time
 */
public record UserResponse(Long id, String username, String email,
        String displayName, int playlistCount, int likedSongCount,
        int followedArtistCount, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(),
                user.getEmail(), user.getDisplayName(),
                user.getPlaylists().size(), user.getLikedSongs().size(),
                user.getFollowedArtists().size(), user.getCreatedAt());
    }
}
