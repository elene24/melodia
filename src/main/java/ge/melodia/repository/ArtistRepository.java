/*
 * ArtistRepository.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Data access for artists.
 */
package ge.melodia.repository;

import java.util.List;

import ge.melodia.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link Artist}.
 */
@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    /* Method-name query with pagination: name LIKE %?% */
    Page<Artist> findByNameContainingIgnoreCase(String name,
            Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Artist> findAllByOrderByNameAsc();

    /**
     * JPQL query: artists ordered by how many users follow them.
     *
     * @param pageable limits the number of results
     * @return most followed artists first
     */
    @Query("SELECT a FROM User u JOIN u.followedArtists a "
            + "GROUP BY a ORDER BY COUNT(u) DESC, a.name ASC")
    List<Artist> findMostFollowed(Pageable pageable);

    /**
     * Removes all follows of an artist before the artist is deleted.
     *
     * @param artistId artist id
     */
    @Modifying
    @Query(value = "DELETE FROM user_followed_artists "
            + "WHERE artist_id = :artistId", nativeQuery = true)
    void deleteFollows(@Param("artistId") Long artistId);
}
