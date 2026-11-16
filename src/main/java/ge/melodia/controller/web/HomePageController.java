/*
 * HomePageController.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Home page of the website.
 */
package ge.melodia.controller.web;

import ge.melodia.service.AlbumService;
import ge.melodia.service.GenreService;
import ge.melodia.service.SongService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Shows trending songs, the newest albums and genre statistics.
 */
@Controller
public class HomePageController {

    private final SongService songService;
    private final AlbumService albumService;
    private final GenreService genreService;

    public HomePageController(SongService songService,
            AlbumService albumService, GenreService genreService) {
        this.songService = songService;
        this.albumService = albumService;
        this.genreService = genreService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("trending", songService.findTrending());
        model.addAttribute("latestAlbums", albumService.findLatest());
        model.addAttribute("genreStats", genreService.getStatistics());
        model.addAttribute("mostLiked", songService.findMostLiked(null, 5));
        return "index";
    }
}
