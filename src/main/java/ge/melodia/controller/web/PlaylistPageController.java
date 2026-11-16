/*
 * PlaylistPageController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Playlist pages: list, create form and details.
 */
package ge.melodia.controller.web;

import ge.melodia.dto.PlaylistRequest;
import ge.melodia.dto.PlaylistResponse;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.service.PlaylistService;
import ge.melodia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HTML pages for playlists.
 */
@Controller
@RequestMapping("/playlists")
public class PlaylistPageController {

    private final PlaylistService playlistService;
    private final UserService userService;

    public PlaylistPageController(PlaylistService playlistService,
            UserService userService) {
        this.playlistService = playlistService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@PageableDefault(size = 12, sort = "updatedAt",
            direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        model.addAttribute("playlistForm", new PlaylistRequest());
        addListData(pageable, model);
        return "playlists/list";
    }

    /**
     * Creates a playlist for the selected user.
     *
     * @param ownerId       id of the user who owns the playlist
     * @param form          validated form fields
     * @param bindingResult validation result
     * @param pageable      used to show the list again on errors
     * @param model         view model
     * @param redirect      flash attributes
     * @return template name or redirect
     */
    @PostMapping
    public String create(@RequestParam Long ownerId,
            @Valid @ModelAttribute("playlistForm") PlaylistRequest form,
            BindingResult bindingResult,
            @PageableDefault(size = 12, sort = "updatedAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            Model model, RedirectAttributes redirect) {
        if (!bindingResult.hasErrors()) {
            try {
                PlaylistResponse playlist =
                        playlistService.create(ownerId, form);
                redirect.addFlashAttribute("message",
                        "Playlist '" + playlist.name() + "' was created");
                return "redirect:/playlists/" + playlist.id();
            } catch (DuplicateResourceException e) {
                bindingResult.rejectValue("name", "duplicate",
                        e.getMessage());
            }
        }
        model.addAttribute("selectedOwnerId", ownerId);
        addListData(pageable, model);
        return "playlists/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("playlist", playlistService.findById(id));
        return "playlists/detail";
    }

    @PostMapping("/{id}/songs/{songId}/remove")
    public String removeSong(@PathVariable Long id,
            @PathVariable Long songId, RedirectAttributes redirect) {
        playlistService.removeSong(id, songId);
        redirect.addFlashAttribute("message", "Removed from the playlist");
        return "redirect:/playlists/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        playlistService.delete(id);
        redirect.addFlashAttribute("message", "The playlist was deleted");
        return "redirect:/playlists";
    }

    private void addListData(Pageable pageable, Model model) {
        model.addAttribute("playlists",
                playlistService.search(null, pageable));
        model.addAttribute("users", userService.findAllForSelect());
    }
}
