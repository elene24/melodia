/*
 * GenreService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Business logic for genres.
 */
package ge.melodia.service;

import java.util.List;

import ge.melodia.dto.GenreRequest;
import ge.melodia.dto.GenreResponse;
import ge.melodia.dto.GenreStatsView;
import ge.melodia.entity.Genre;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.exception.ResourceNotFoundException;
import ge.melodia.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates, lists and deletes genres.
 */
@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        return genreRepository.findAllByOrderByNameAsc().stream()
                .map(GenreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GenreStatsView> getStatistics() {
        return genreRepository.countSongsPerGenre();
    }

    @Transactional
    public GenreResponse create(GenreRequest request) {
        String name = request.getName().trim();
        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(
                    "Genre '" + name + "' already exists");
        }
        return GenreResponse.from(genreRepository.save(new Genre(name)));
    }

    @Transactional
    public void delete(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", id));
        genreRepository.detachFromSongs(id);
        genreRepository.delete(genre);
    }
}
