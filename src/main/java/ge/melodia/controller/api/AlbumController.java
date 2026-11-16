/*
 * AlbumController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * REST endpoints for albums.
 */
package ge.melodia.controller.api;

import java.util.List;

import ge.melodia.dto.AlbumRequest;
import ge.melodia.dto.AlbumResponse;
import ge.melodia.dto.SongResponse;
import ge.melodia.service.AlbumService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * Album API.
 */
@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public Page<AlbumResponse> list(
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "releaseDate",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return albumService.search(title, pageable);
    }

    @GetMapping("/{id}")
    public AlbumResponse get(@PathVariable Long id) {
        return albumService.findById(id);
    }

    @GetMapping("/{id}/songs")
    public List<SongResponse> songs(@PathVariable Long id) {
        return albumService.findSongs(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumResponse create(@Valid @RequestBody AlbumRequest request) {
        return albumService.create(request);
    }

    @PutMapping("/{id}")
    public AlbumResponse update(@PathVariable Long id,
            @Valid @RequestBody AlbumRequest request) {
        return albumService.update(id, request);
    }

    /**
     * Uploads the album cover (multipart/form-data, part "file").
     *
     * @param id   album id
     * @param file image file
     * @return the updated album
     */
    @PostMapping(value = "/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AlbumResponse uploadCover(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return albumService.uploadCover(id, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        albumService.delete(id);
    }
}
