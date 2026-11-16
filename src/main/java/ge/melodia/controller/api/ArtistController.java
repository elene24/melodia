/*
 * ArtistController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * REST endpoints for artists.
 */
package ge.melodia.controller.api;

import java.util.List;

import ge.melodia.dto.AlbumResponse;
import ge.melodia.dto.ArtistRequest;
import ge.melodia.dto.ArtistResponse;
import ge.melodia.service.ArtistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Artist API.
 */
@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    @GetMapping
    public Page<ArtistResponse> list(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return artistService.search(name, pageable);
    }

    @GetMapping("/most-followed")
    public List<ArtistResponse> mostFollowed(
            @RequestParam(defaultValue = "5") int limit) {
        return artistService.findMostFollowed(limit);
    }

    @GetMapping("/{id}")
    public ArtistResponse get(@PathVariable Long id) {
        return artistService.findById(id);
    }

    @GetMapping("/{id}/albums")
    public List<AlbumResponse> albums(@PathVariable Long id) {
        return artistService.findAlbums(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtistResponse create(@Valid @RequestBody ArtistRequest request) {
        return artistService.create(request);
    }

    @PutMapping("/{id}")
    public ArtistResponse update(@PathVariable Long id,
            @Valid @RequestBody ArtistRequest request) {
        return artistService.update(id, request);
    }

    /**
     * Uploads the artist photo (multipart/form-data, part "file").
     *
     * @param id   artist id
     * @param file image file
     * @return the updated artist
     */
    @PostMapping(value = "/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ArtistResponse uploadImage(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return artistService.uploadImage(id, file);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        artistService.delete(id);
    }
}
