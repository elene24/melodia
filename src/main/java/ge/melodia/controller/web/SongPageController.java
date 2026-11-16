/*
 * SongPageController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Song pages: list, upload form and details.
 */
package ge.melodia.controller.web;

import ge.melodia.dto.SongRequest;
import ge.melodia.dto.SongResponse;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.exception.InvalidFileException;
import ge.melodia.service.AlbumService;
import ge.melodia.service.GenreService;
import ge.melodia.service.PlaylistService;
import ge.melodia.service.SongService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HTML pages for browsing and uploading songs.
 */
@Controller
@RequestMapping("/songs")
public class SongPageController {

    private final SongService songService;
    private final AlbumService albumService;
    private final GenreService genreService;
    private final PlaylistService playlistService;

    public SongPageController(SongService songService,
            AlbumService albumService, GenreService genreService,
            PlaylistService playlistService) {
        this.songService = songService;
        this.albumService = albumService;
        this.genreService = genreService;
        this.playlistService = playlistService;
    }

    /**
     * Song list with search, genre filter, sorting and pagination.
     *
     * @param query    search text
     * @param genre    genre filter
     * @param pageable page, size and sort from the query string
     * @param model    view model
     * @return template name
     */
    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(required = false) String genre,
            @PageableDefault(size = 10, sort = "title") Pageable pageable,
            Model model) {
        Page<SongResponse> songs = songService.search(query, genre, pageable);
        String sort = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.getProperty() + ","
                        + order.getDirection().name().toLowerCase())
                .orElse("title,asc");

        model.addAttribute("songs", songs);
        model.addAttribute("q", query == null ? "" : query);
        model.addAttribute("genre", genre == null ? "" : genre);
        model.addAttribute("sort", sort);
        model.addAttribute("genres", genreService.findAll());
        return "songs/list";
    }

    @GetMapping("/new")
    public String uploadForm(
            @RequestParam(required = false) Long albumId, Model model) {
        SongRequest form = new SongRequest();
        form.setAlbumId(albumId);
        model.addAttribute("songForm", form);
        addFormData(model);
        return "songs/form";
    }

    /**
     * Handles the multipart upload form. On errors the form is shown
     * again; on success the browser is redirected (Post/Redirect/Get).
     *
     * @param form          validated form fields
     * @param bindingResult validation result
     * @param audioFile     uploaded audio file
     * @param model         view model
     * @param redirect      flash attributes for the next page
     * @return template name or redirect
     */
    @PostMapping("/new")
    public String upload(@Valid @ModelAttribute("songForm") SongRequest form,
            BindingResult bindingResult,
            @RequestParam(name = "audioFile", required = false)
            MultipartFile audioFile,
            Model model, RedirectAttributes redirect) {
        if (audioFile == null || audioFile.isEmpty()) {
            model.addAttribute("fileError", "Please choose an audio file");
        }
        if (bindingResult.hasErrors() || model.containsAttribute("fileError")) {
            addFormData(model);
            return "songs/form";
        }

        try {
            SongResponse song = songService.create(form, audioFile);
            redirect.addFlashAttribute("message",
                    "'" + song.title() + "' was uploaded");
            return "redirect:/songs/" + song.id();
        } catch (InvalidFileException e) {
            model.addAttribute("fileError", e.getMessage());
            addFormData(model);
            return "songs/form";
        }
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("song", songService.findById(id));
        model.addAttribute("playlists", playlistService.findAllForSelect());
        return "songs/detail";
    }

    @PostMapping("/{id}/playlist")
    public String addToPlaylist(@PathVariable Long id,
            @RequestParam Long playlistId, RedirectAttributes redirect) {
        try {
            playlistService.addSong(playlistId, id);
            redirect.addFlashAttribute("message", "Added to the playlist");
        } catch (DuplicateResourceException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/songs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        songService.delete(id);
        redirect.addFlashAttribute("message", "The song was deleted");
        return "redirect:/songs";
    }

    private void addFormData(Model model) {
        model.addAttribute("albums", albumService.findAllForSelect());
        model.addAttribute("genres", genreService.findAll());
    }
}
