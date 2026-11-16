/*
 * ArtistPageController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Artist pages: list, create form and details.
 */
package ge.melodia.controller.web;

import ge.melodia.dto.AlbumRequest;
import ge.melodia.dto.AlbumResponse;
import ge.melodia.dto.ArtistRequest;
import ge.melodia.dto.ArtistResponse;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.exception.InvalidFileException;
import ge.melodia.service.AlbumService;
import ge.melodia.service.ArtistService;
import jakarta.validation.Valid;
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
 * HTML pages for artists and creating their albums.
 */
@Controller
@RequestMapping("/artists")
public class ArtistPageController {

    private final ArtistService artistService;
    private final AlbumService albumService;

    public ArtistPageController(ArtistService artistService,
            AlbumService albumService) {
        this.artistService = artistService;
        this.albumService = albumService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 12, sort = "name") Pageable pageable,
            Model model) {
        model.addAttribute("artistForm", new ArtistRequest());
        addListData(q, pageable, model);
        return "artists/list";
    }

    /**
     * Creates an artist with an optional photo (multipart form).
     *
     * @param form          validated form fields
     * @param bindingResult validation result
     * @param image         optional photo
     * @param pageable      used to show the list again on errors
     * @param model         view model
     * @param redirect      flash attributes
     * @return template name or redirect
     */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("artistForm") ArtistRequest form,
            BindingResult bindingResult,
            @RequestParam(name = "image", required = false)
            MultipartFile image,
            @PageableDefault(size = 12, sort = "name") Pageable pageable,
            Model model, RedirectAttributes redirect) {
        if (!bindingResult.hasErrors()) {
            try {
                ArtistResponse artist = artistService.create(form, image);
                redirect.addFlashAttribute("message",
                        "Artist '" + artist.name() + "' was created");
                return "redirect:/artists/" + artist.id();
            } catch (DuplicateResourceException e) {
                bindingResult.rejectValue("name", "duplicate",
                        e.getMessage());
            } catch (InvalidFileException e) {
                model.addAttribute("fileError", e.getMessage());
            }
        }
        addListData(null, pageable, model);
        return "artists/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        AlbumRequest albumForm = new AlbumRequest();
        albumForm.setArtistId(id);
        model.addAttribute("albumForm", albumForm);
        addDetailData(id, model);
        return "artists/detail";
    }

    /**
     * Creates an album for the artist with an optional cover image.
     *
     * @param id            artist id
     * @param form          validated form fields
     * @param bindingResult validation result
     * @param cover         optional cover image
     * @param model         view model
     * @param redirect      flash attributes
     * @return template name or redirect
     */
    @PostMapping("/{id}/albums")
    public String createAlbum(@PathVariable Long id,
            @Valid @ModelAttribute("albumForm") AlbumRequest form,
            BindingResult bindingResult,
            @RequestParam(name = "cover", required = false)
            MultipartFile cover,
            Model model, RedirectAttributes redirect) {
        form.setArtistId(id);
        if (!bindingResult.hasErrors()) {
            try {
                AlbumResponse album = albumService.create(form, cover);
                redirect.addFlashAttribute("message",
                        "Album '" + album.title() + "' was created");
                return "redirect:/albums/" + album.id();
            } catch (InvalidFileException e) {
                model.addAttribute("fileError", e.getMessage());
            }
        }
        addDetailData(id, model);
        return "artists/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        artistService.delete(id);
        redirect.addFlashAttribute("message", "The artist was deleted");
        return "redirect:/artists";
    }

    private void addListData(String q, Pageable pageable, Model model) {
        model.addAttribute("artists", artistService.search(q, pageable));
        model.addAttribute("q", q == null ? "" : q);
    }

    private void addDetailData(Long id, Model model) {
        model.addAttribute("artist", artistService.findById(id));
        model.addAttribute("albums", artistService.findAlbums(id));
    }
}
