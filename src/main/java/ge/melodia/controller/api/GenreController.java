/*
 * GenreController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * REST endpoints for genres.
 */
package ge.melodia.controller.api;

import java.util.List;

import ge.melodia.dto.GenreRequest;
import ge.melodia.dto.GenreResponse;
import ge.melodia.dto.GenreStatsView;
import ge.melodia.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Genre API.
 */
@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public List<GenreResponse> list() {
        return genreService.findAll();
    }

    @GetMapping("/stats")
    public List<GenreStatsView> stats() {
        return genreService.getStatistics();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenreResponse create(@Valid @RequestBody GenreRequest request) {
        return genreService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        genreService.delete(id);
    }
}
