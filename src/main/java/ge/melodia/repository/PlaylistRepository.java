/*
 * PlaylistRepository.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Data access for playlists.
 */
package ge.melodia.repository;

import java.util.List;

import ge.melodia.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link Playlist}.
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    /* Method-name query: playlists of one user, newest first */
    List<Playlist> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    /* Method-name query with pagination */
    Page<Playlist> findByNameContainingIgnoreCase(String name,
            Pageable pageable);

    /* Method-name query with "And": duplicate name check per user */
    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    /**
     * JPQL aggregation: total length of a playlist in seconds.
     *
     * @param playlistId playlist id
     * @return sum of song durations (0 for an empty playlist)
     */
    @Query("SELECT COALESCE(SUM(s.durationSeconds), 0) "
            + "FROM Playlist p JOIN p.songs s WHERE p.id = :playlistId")
    Long sumDurationSeconds(@Param("playlistId") Long playlistId);
}
