/*
 * AlbumPageController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Album detail page.
 */
package ge.melodia.controller.web;

import ge.melodia.dto.AlbumResponse;
import ge.melodia.service.AlbumService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Shows an album with its track list.
 */
@Controller
@RequestMapping("/albums")
public class AlbumPageController {

    private final AlbumService albumService;

    public AlbumPageController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("album", albumService.findById(id));
        model.addAttribute("songs", albumService.findSongs(id));
        return "albums/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        AlbumResponse album = albumService.findById(id);
        albumService.delete(id);
        redirect.addFlashAttribute("message",
                "Album '" + album.title() + "' was deleted");
        return "redirect:/artists/" + album.artistId();
    }
}
