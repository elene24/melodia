/*
 * SongRepository.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Data access for songs.
 */
package ge.melodia.repository;

import java.util.Collection;
import java.util.List;

import ge.melodia.dto.SongStatsResponse;
import ge.melodia.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link Song}.
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    /*
     * Method-name query with "And": songs of a genre that are not
     * longer than the given number of seconds.
     */
    List<Song> findByGenresNameIgnoreCaseAndDurationSecondsLessThanEqual(
            String genreName, int maxDurationSeconds, Sort sort);

    /* Method-name query: songs of an album */
    List<Song> findByAlbumIdOrderByIdAsc(Long albumId);

    /* Method-name query with a limit: "Trending" chart */
    List<Song> findTop10ByOrderByPlayCountDesc();

    /**
     * JPQL search by song title or artist name, optionally filtered by
     * genre. Supports pagination and sorting.
     *
     * @param query    text to search for ("" matches everything)
     * @param genre    genre name ("" means any genre)
     * @param pageable page number, size and sort
     * @return one page of songs
     */
    @Query(value = "SELECT s FROM Song s "
            + "JOIN s.album al JOIN al.artist ar "
            + "WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(ar.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "AND (:genre = '' OR EXISTS (SELECT g FROM Genre g "
            + "WHERE g MEMBER OF s.genres "
            + "AND LOWER(g.name) = LOWER(:genre)))",
            countQuery = "SELECT COUNT(s) FROM Song s "
            + "JOIN s.album al JOIN al.artist ar "
            + "WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(ar.name) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "AND (:genre = '' OR EXISTS (SELECT g FROM Genre g "
            + "WHERE g MEMBER OF s.genres "
            + "AND LOWER(g.name) = LOWER(:genre)))")
    Page<Song> search(@Param("query") String query,
            @Param("genre") String genre, Pageable pageable);

    /**
     * Atomic UPDATE: the database increments the counter itself, so two
     * simultaneous plays can never overwrite each other.
     *
     * @param songId song id
     * @return number of updated rows (0 if the song does not exist)
     */
    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 "
            + "WHERE s.id = :songId")
    int incrementPlayCount(@Param("songId") Long songId);

    /**
     * JPQL query: songs liked by a user (paginated).
     *
     * @param userId   user id
     * @param pageable page number, size and sort
     * @return one page of liked songs
     */
    @Query("SELECT s FROM Song s WHERE EXISTS (SELECT u FROM User u "
            + "WHERE u.id = :userId AND s MEMBER OF u.likedSongs)")
    Page<Song> findLikedByUser(@Param("userId") Long userId,
            Pageable pageable);

    /**
     * JPQL query: recommendations - songs of artists the user follows
     * that the user has not liked yet, most played first.
     *
     * @param userId   user id
     * @param pageable limits the number of results
     * @return recommended songs
     */
    @Query("SELECT s FROM Song s "
            + "WHERE s.album.artist IN (SELECT a FROM User u "
            + "JOIN u.followedArtists a WHERE u.id = :userId) "
            + "AND s NOT IN (SELECT ls FROM User u2 "
            + "JOIN u2.likedSongs ls WHERE u2.id = :userId) "
            + "ORDER BY s.playCount DESC")
    List<Song> findRecommendedForUser(@Param("userId") Long userId,
            Pageable pageable);

    /**
     * JPQL aggregation with GROUP BY and a constructor expression:
     * the most liked songs, optionally only in one genre.
     *
     * @param genre    genre name ("" means any genre)
     * @param pageable limits the number of results
     * @return like statistics, most liked first
     */
    @Query("SELECT new ge.melodia.dto.SongStatsResponse("
            + "s.id, s.title, ar.name, COUNT(u)) "
            + "FROM User u JOIN u.likedSongs s "
            + "JOIN s.album al JOIN al.artist ar "
            + "WHERE (:genre = '' OR EXISTS (SELECT g FROM Genre g "
            + "WHERE g MEMBER OF s.genres "
            + "AND LOWER(g.name) = LOWER(:genre))) "
            + "GROUP BY s.id, s.title, ar.name "
            + "ORDER BY COUNT(u) DESC, s.title ASC")
    List<SongStatsResponse> findMostLiked(@Param("genre") String genre,
            Pageable pageable);

    /**
     * Removes songs from every playlist (used before deleting them).
     *
     * @param songIds ids of the songs
     */
    @Modifying
    @Query(value = "DELETE FROM playlist_songs WHERE song_id IN (:songIds)",
            nativeQuery = true)
    void removeFromAllPlaylists(@Param("songIds") Collection<Long> songIds);

    /**
     * Removes songs from every user's liked songs (used before deleting
     * them).
     *
     * @param songIds ids of the songs
     */
    @Modifying
    @Query(value = "DELETE FROM user_liked_songs "
            + "WHERE song_id IN (:songIds)", nativeQuery = true)
    void removeFromAllLikes(@Param("songIds") Collection<Long> songIds);
}
