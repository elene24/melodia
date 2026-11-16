/*
 * SongController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * REST endpoints for songs.
 */
package ge.melodia.controller.api;

import java.util.List;
import java.util.Map;

import ge.melodia.dto.SongRequest;
import ge.melodia.dto.SongResponse;
import ge.melodia.dto.SongStatsResponse;
import ge.melodia.service.SongService;
import ge.melodia.service.SongService.SongAudio;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Song API: upload, search with pagination and sorting, streaming and
 * charts.
 */
@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    /**
     * Lists songs, for example
     * {@code /api/songs?q=night&genre=Pop&page=0&size=5&sort=playCount,desc}.
     *
     * @param query    optional text matched against title and artist
     * @param genre    optional genre name
     * @param pageable page, size and sort taken from the query string
     * @return one page of songs
     */
    @GetMapping
    public Page<SongResponse> list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String genre,
            @PageableDefault(size = 10, sort = "title",
                    direction = Sort.Direction.ASC) Pageable pageable) {
        return songService.search(query, genre, pageable);
    }

    @GetMapping("/{id}")
    public SongResponse get(@PathVariable Long id) {
        return songService.findById(id);
    }

    @GetMapping("/trending")
    public List<SongResponse> trending() {
        return songService.findTrending();
    }

    @GetMapping("/short")
    public List<SongResponse> shortSongs(@RequestParam String genre,
            @RequestParam(defaultValue = "180") int maxDuration) {
        return songService.findShortSongs(genre, maxDuration);
    }

    @GetMapping("/most-liked")
    public List<SongStatsResponse> mostLiked(
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "10") int limit) {
        return songService.findMostLiked(genre, limit);
    }

    /**
     * Uploads a song. Must be sent as multipart/form-data: the text
     * fields are bound to {@link SongRequest} and the audio file is the
     * "audioFile" part.
     *
     * @param request   validated song metadata
     * @param audioFile the audio file
     * @return the created song
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SongResponse upload(@Valid @ModelAttribute SongRequest request,
            @RequestParam("audioFile") MultipartFile audioFile) {
        return songService.create(request, audioFile);
    }

    @PutMapping("/{id}")
    public SongResponse update(@PathVariable Long id,
            @Valid @RequestBody SongRequest request) {
        return songService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        songService.delete(id);
    }

    /**
     * Counts one play of the song.
     *
     * @param id song id
     * @return the new play count
     */
    @PostMapping("/{id}/play")
    public Map<String, Long> play(@PathVariable Long id) {
        return Map.of("playCount", songService.registerPlay(id));
    }

    /**
     * Streams the audio file. Spring supports HTTP Range requests for
     * {@link Resource} bodies, so the browser can seek in the track.
     *
     * @param id song id
     * @return the audio file
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> stream(@PathVariable Long id) {
        SongAudio audio = songService.loadAudio(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(audio.contentType()))
                .body(audio.resource());
    }
}
