/*
 * UserController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * REST endpoints for users, likes and follows.
 */
package ge.melodia.controller.api;

import java.util.List;

import ge.melodia.dto.PlaylistRequest;
import ge.melodia.dto.PlaylistResponse;
import ge.melodia.dto.PlaylistSummaryResponse;
import ge.melodia.dto.SongResponse;
import ge.melodia.dto.UserRequest;
import ge.melodia.dto.UserResponse;
import ge.melodia.service.PlaylistService;
import ge.melodia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * User API, including liked songs, followed artists, recommendations
 * and the user's playlists.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PlaylistService playlistService;

    public UserController(UserService userService,
            PlaylistService playlistService) {
        this.userService = userService;
        this.playlistService = playlistService;
    }

    @GetMapping
    public Page<UserResponse> list(
            @PageableDefault(size = 10, sort = "username") Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/{id}/liked-songs")
    public Page<SongResponse> likedSongs(@PathVariable Long id,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return userService.findLikedSongs(id, pageable);
    }

    @PostMapping("/{id}/liked-songs/{songId}")
    public UserResponse like(@PathVariable Long id,
            @PathVariable Long songId) {
        return userService.likeSong(id, songId);
    }

    @DeleteMapping("/{id}/liked-songs/{songId}")
    public UserResponse unlike(@PathVariable Long id,
            @PathVariable Long songId) {
        return userService.unlikeSong(id, songId);
    }

    @PostMapping("/{id}/followed-artists/{artistId}")
    public UserResponse follow(@PathVariable Long id,
            @PathVariable Long artistId) {
        return userService.followArtist(id, artistId);
    }

    @DeleteMapping("/{id}/followed-artists/{artistId}")
    public UserResponse unfollow(@PathVariable Long id,
            @PathVariable Long artistId) {
        return userService.unfollowArtist(id, artistId);
    }

    @GetMapping("/{id}/recommendations")
    public List<SongResponse> recommendations(@PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit) {
        return userService.findRecommendations(id, limit);
    }

    @GetMapping("/{id}/playlists")
    public List<PlaylistSummaryResponse> playlists(@PathVariable Long id) {
        return playlistService.findByOwner(id);
    }

    @PostMapping("/{id}/playlists")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse createPlaylist(@PathVariable Long id,
            @Valid @RequestBody PlaylistRequest request) {
        return playlistService.create(id, request);
    }
}
