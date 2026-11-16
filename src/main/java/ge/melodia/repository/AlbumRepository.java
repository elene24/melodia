/*
 * AlbumRepository.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Data access for albums.
 */
package ge.melodia.repository;

import java.util.List;

import ge.melodia.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link Album}.
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    /* Method-name query: albums of one artist, newest first */
    List<Album> findByArtistIdOrderByReleaseDateDesc(Long artistId);

    /* Method-name query with pagination */
    Page<Album> findByTitleContainingIgnoreCase(String title,
            Pageable pageable);

    /* Method-name query with a limit: the six newest albums */
    List<Album> findTop6ByOrderByReleaseDateDesc();

    /**
     * JPQL query used to fill drop-down lists, ordered by artist and
     * title. The artist is fetched in the same query.
     *
     * @return all albums
     */
    @Query("SELECT al FROM Album al JOIN FETCH al.artist ar "
            + "ORDER BY ar.name, al.title")
    List<Album> findAllWithArtist();
}
