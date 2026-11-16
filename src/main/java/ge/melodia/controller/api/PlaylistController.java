/*
 * PlaylistController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * REST endpoints for playlists.
 */
package ge.melodia.controller.api;

import ge.melodia.dto.PlaylistRequest;
import ge.melodia.dto.PlaylistResponse;
import ge.melodia.dto.PlaylistSummaryResponse;
import ge.melodia.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Playlist API. Playlists are created through
 * {@code POST /api/users/{userId}/playlists}.
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public Page<PlaylistSummaryResponse> list(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "updatedAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return playlistService.search(name, pageable);
    }

    @GetMapping("/{id}")
    public PlaylistResponse get(@PathVariable Long id) {
        return playlistService.findById(id);
    }

    @PutMapping("/{id}")
    public PlaylistResponse update(@PathVariable Long id,
            @Valid @RequestBody PlaylistRequest request) {
        return playlistService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        playlistService.delete(id);
    }

    @PostMapping("/{id}/songs/{songId}")
    public PlaylistResponse addSong(@PathVariable Long id,
            @PathVariable Long songId) {
        return playlistService.addSong(id, songId);
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public PlaylistResponse removeSong(@PathVariable Long id,
            @PathVariable Long songId) {
        return playlistService.removeSong(id, songId);
    }
}
