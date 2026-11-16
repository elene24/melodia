/*
 * SeedDataService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Fills an empty database with demo data.
 */
package ge.melodia.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ge.melodia.entity.Album;
import ge.melodia.entity.Artist;
import ge.melodia.entity.Genre;
import ge.melodia.entity.Playlist;
import ge.melodia.entity.Song;
import ge.melodia.entity.User;
import ge.melodia.exception.FileStorageException;
import ge.melodia.repository.ArtistRepository;
import ge.melodia.repository.GenreRepository;
import ge.melodia.repository.PlaylistRepository;
import ge.melodia.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates demo artists, albums, songs, users and playlists so the
 * application can be demonstrated right after the first start. The
 * audio clips are 30-second segments of royalty-free tracks from
 * Pixabay (Pixabay Content License), credited to their uploaders.
 * They are stored under resources/seed and copied into the upload
 * folder like real uploads. Album groupings and dates are demo values.
 */
@Service
public class SeedDataService {

    /** Length of the demo audio clips in seconds. */
    private static final int SEGMENT_SECONDS = 30;
    private static final String PIXABAY_BIO = "Music published on Pixabay "
            + "under the Pixabay Content License.";

    private static final Logger LOG =
            LoggerFactory.getLogger(SeedDataService.class);

    private final GenreRepository genreRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final FileStorageService fileStorageService;

    private final Map<String, Genre> genres = new HashMap<>();
    private final Map<String, Song> songs = new HashMap<>();

    public SeedDataService(GenreRepository genreRepository,
                           ArtistRepository artistRepository, UserRepository userRepository,
                           PlaylistRepository playlistRepository,
                           FileStorageService fileStorageService) {
        this.genreRepository = genreRepository;
        this.artistRepository = artistRepository;
        this.userRepository = userRepository;
        this.playlistRepository = playlistRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Inserts the demo data if there are no artists yet.
     */
    @Transactional
    public void seedIfEmpty() {
        if (artistRepository.count() > 0) {
            return;
        }
        LOG.info("Empty database detected - inserting demo data");

        for (String name : Arrays.asList("Reggae", "Dub", "Pop",
                "Afrobeat", "Electronic", "Rock", "Jazz", "Hip-Hop")) {
            genres.put(name, genreRepository.save(new Genre(name)));
        }

        Artist brotheration = artist("Brotheration Records",
                "brotheration-records.jpg", PIXABAY_BIO);
        Album volumeOne = album(brotheration, "Brotheration Selection 1",
                LocalDate.of(2025, 3, 14), "brotheration-vol-1.jpg");
        song(volumeOne, "Su-Su Riddim", "su-su-riddim.mp3", 2140,
                "Reggae", "Dub");
        song(volumeOne, "Locust", "locust.mp3", 1180, "Reggae");
        song(volumeOne, "Rain Fall", "rain-fall.mp3", 905, "Reggae");
        Album volumeTwo = album(brotheration, "Brotheration Selection 2",
                LocalDate.of(2025, 6, 20), "brotheration-vol-2.jpg");
        song(volumeTwo, "Everyday Is A Holiday",
                "everyday-is-a-holiday.mp3", 1765, "Reggae", "Pop");
        song(volumeTwo, "Love Of My Life", "love-of-my-life.mp3", 1320,
                "Reggae");

        Artist elijah = artist("Elijah_K", "elijah-k.jpg", PIXABAY_BIO);
        song(album(elijah, "Elijah_K Singles", LocalDate.of(2025, 1, 10),
                        "elijah-k-singles.jpg"), "Summer", "summer.mp3", 1540,
                "Reggae", "Pop");

        Artist kond = artist("Kond1", "kond1.jpg", PIXABAY_BIO);
        song(album(kond, "Kond1 Singles", LocalDate.of(2024, 11, 5),
                        "kond1-singles.jpg"), "Rasta Never Fall",
                "rasta-never-fall.mp3", 870, "Reggae");

        Artist julius = artist("JuliusH", "juliush.jpg", PIXABAY_BIO);
        song(album(julius, "JuliusH Singles", LocalDate.of(2024, 9, 18),
                        "juliush-singles.jpg"), "Reggae Town", "reggae-town.mp3",
                1095, "Reggae", "Dub");

        Artist alex = artist("AlexGuz", "alexguz.jpg", PIXABAY_BIO);
        song(album(alex, "AlexGuz Singles", LocalDate.of(2025, 7, 2),
                        "alexguz-singles.jpg"), "Reggae", "reggae.mp3", 640,
                "Reggae");

        Artist oibeats = artist("OiBeats", "oibeats.jpg", PIXABAY_BIO);
        song(album(oibeats, "OiBeats Singles", LocalDate.of(2025, 2, 21),
                        "oibeats-singles.jpg"), "Summer Afro Tropical Instrumental",
                "summer-afro-tropical.mp3", 1410, "Afrobeat", "Electronic");

        Artist tunetank = artist("Tunetank", "tunetank.jpg", PIXABAY_BIO);
        song(album(tunetank, "Tunetank Singles", LocalDate.of(2025, 8, 12),
                        "tunetank-singles.jpg"), "Fun Kids Reggae Music",
                "fun-kids-reggae.mp3", 520, "Reggae", "Pop");

        for (Artist artist : Arrays.asList(brotheration, elijah, kond,
                julius, alex, oibeats, tunetank)) {
            artistRepository.save(artist);
        }

        User nino = new User("nino_k", "nino@example.com", "Nino K.");
        nino.getFollowedArtists().add(brotheration);
        nino.getFollowedArtists().add(oibeats);
        nino.getLikedSongs().add(songs.get("Su-Su Riddim"));
        nino.getLikedSongs().add(songs.get("Summer"));
        nino.getLikedSongs().add(songs.get("Reggae Town"));

        User giorgi = new User("giorgi.m", "giorgi@example.com",
                "Giorgi M.");
        giorgi.getFollowedArtists().add(julius);
        giorgi.getFollowedArtists().add(kond);
        giorgi.getLikedSongs().add(songs.get("Su-Su Riddim"));
        giorgi.getLikedSongs().add(songs.get("Rasta Never Fall"));
        giorgi.getLikedSongs().add(songs.get("Everyday Is A Holiday"));

        User demo = new User("demo", "demo@example.com", "Demo Listener");
        demo.getFollowedArtists().add(brotheration);
        demo.getLikedSongs().add(songs.get("Su-Su Riddim"));
        demo.getLikedSongs().add(songs.get("Locust"));

        for (User user : Arrays.asList(nino, giorgi, demo)) {
            userRepository.save(user);
        }

        Playlist sunny = new Playlist("Sunny Morning",
                "Light tracks for the first coffee", nino);
        sunny.getSongs().add(songs.get("Summer"));
        sunny.getSongs().add(songs.get("Summer Afro Tropical Instrumental"));
        sunny.getSongs().add(songs.get("Everyday Is A Holiday"));

        Playlist dub = new Playlist("Dub Session",
                "Deep bass and echoes", giorgi);
        dub.getSongs().add(songs.get("Reggae Town"));
        dub.getSongs().add(songs.get("Su-Su Riddim"));
        dub.getSongs().add(songs.get("Rain Fall"));
        dub.getSongs().add(songs.get("Rasta Never Fall"));

        playlistRepository.save(sunny);
        playlistRepository.save(dub);
        LOG.info("Demo data inserted: 7 artists, 11 songs, 3 users");
    }

    private Artist artist(String name, String image, String bio) {
        Artist artist = new Artist(name, bio);
        artist.setImageFileName(copySeedFile("images/" + image, "jpg",
                false));
        return artist;
    }

    private Album album(Artist artist, String title, LocalDate released,
                        String cover) {
        Album album = new Album(title, released, artist);
        album.setCoverFileName(copySeedFile("images/" + cover, "jpg",
                false));
        artist.getAlbums().add(album);
        return album;
    }

    private void song(Album album, String title, String audio,
                      long playCount, String... genreNames) {
        Song song = new Song();
        song.setTitle(title);
        song.setDurationSeconds(SEGMENT_SECONDS);
        song.setPlayCount(playCount);
        song.setAlbum(album);
        song.setOriginalFileName(audio);
        song.setAudioFileName(copySeedFile("audio/" + audio, "mp3", true));
        for (String genreName : genreNames) {
            song.getGenres().add(genres.get(genreName));
        }
        album.getSongs().add(song);
        songs.put(title, song);
    }

    private String copySeedFile(String path, String extension,
                                boolean audio) {
        ClassPathResource resource = new ClassPathResource("seed/" + path);
        try (InputStream in = resource.getInputStream()) {
            return fileStorageService.storeStream(in, extension, audio);
        } catch (IOException e) {
            throw new FileStorageException("Missing seed file " + path, e);
        }
    }
}
