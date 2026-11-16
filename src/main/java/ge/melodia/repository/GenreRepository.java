/*
 * GenreRepository.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Data access for genres.
 */
package ge.melodia.repository;

import java.util.List;
import java.util.Optional;

import ge.melodia.dto.GenreStatsView;
import ge.melodia.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link Genre}.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    /* Method-name query: WHERE LOWER(name) = LOWER(?) */
    Optional<Genre> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Genre> findAllByOrderByNameAsc();

    /**
     * Native SQL query: number of songs in every genre.
     *
     * @return statistics, most popular genre first
     */
    @Query(value = "SELECT g.id AS \"id\", g.name AS \"name\", "
            + "COUNT(sg.song_id) AS \"songCount\" "
            + "FROM genres g "
            + "LEFT JOIN song_genres sg ON sg.genre_id = g.id "
            + "GROUP BY g.id, g.name "
            + "ORDER BY 3 DESC, g.name",
            nativeQuery = true)
    List<GenreStatsView> countSongsPerGenre();

    /**
     * Removes the genre from all songs before the genre is deleted.
     *
     * @param genreId genre id
     */
    @Modifying
    @Query(value = "DELETE FROM song_genres WHERE genre_id = :genreId",
            nativeQuery = true)
    void detachFromSongs(@Param("genreId") Long genreId);
}
