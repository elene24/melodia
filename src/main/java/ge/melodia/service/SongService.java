/*
 * SongService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Business logic for songs: upload, search, streaming and charts.
 */
package ge.melodia.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ge.melodia.dto.SongRequest;
import ge.melodia.dto.SongResponse;
import ge.melodia.dto.SongStatsResponse;
import ge.melodia.entity.Album;
import ge.melodia.entity.Genre;
import ge.melodia.entity.Song;
import ge.melodia.exception.ResourceNotFoundException;
import ge.melodia.repository.AlbumRepository;
import ge.melodia.repository.GenreRepository;
import ge.melodia.repository.SongRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Uploads, updates, deletes, searches and streams songs.
 */
@Service
public class SongService {

    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorageService;

    public SongService(SongRepository songRepository,
            AlbumRepository albumRepository, GenreRepository genreRepository,
            FileStorageService fileStorageService) {
        this.songRepository = songRepository;
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Searches songs by title or artist name, optionally in one genre.
     *
     * @param query    search text, may be null
     * @param genre    genre name, may be null
     * @param pageable page number, size and sort
     * @return one page of songs
     */
    @Transactional(readOnly = true)
    public Page<SongResponse> search(String query, String genre,
            Pageable pageable) {
        return songRepository.search(normalize(query), normalize(genre),
                pageable).map(SongResponse::from);
    }

    @Transactional(readOnly = true)
    public SongResponse findById(Long id) {
        return SongResponse.from(getSong(id));
    }

    @Transactional(readOnly = true)
    public List<SongResponse> findTrending() {
        return songRepository.findTop10ByOrderByPlayCountDesc().stream()
                .map(SongResponse::from)
                .toList();
    }

    /**
     * Songs of a genre that are not longer than the given duration,
     * shortest first.
     *
     * @param genre              genre name
     * @param maxDurationSeconds maximum length in seconds
     * @return matching songs
     */
    @Transactional(readOnly = true)
    public List<SongResponse> findShortSongs(String genre,
            int maxDurationSeconds) {
        Sort sort = Sort.by("durationSeconds").ascending()
                .and(Sort.by("title").ascending());
        return songRepository
                .findByGenresNameIgnoreCaseAndDurationSecondsLessThanEqual(
                        genre, maxDurationSeconds, sort)
                .stream()
                .map(SongResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SongStatsResponse> findMostLiked(String genre, int limit) {
        return songRepository.findMostLiked(normalize(genre),
                PageRequest.of(0, limit));
    }

    /**
     * Uploads a new song. The album is checked before the file is
     * written, and the file is removed again if saving fails, so the
     * disk and the database stay consistent.
     *
     * @param request   song metadata
     * @param audioFile uploaded audio file
     * @return the created song
     */
    @Transactional
    public SongResponse create(SongRequest request, MultipartFile audioFile) {
        Album album = getAlbum(request.getAlbumId());
        Set<Genre> genres = getGenres(request.getGenreIds());
        String fileName = fileStorageService.storeAudio(audioFile);

        try {
            Song song = new Song();
            song.setTitle(request.getTitle().trim());
            song.setDurationSeconds(request.getDurationSeconds());
            song.setAlbum(album);
            song.setGenres(genres);
            song.setAudioFileName(fileName);
            song.setOriginalFileName(audioFile.getOriginalFilename());
            album.getSongs().add(song);
            return SongResponse.from(songRepository.saveAndFlush(song));
        } catch (RuntimeException e) {
            fileStorageService.deleteAudio(fileName);
            throw e;
        }
    }

    @Transactional
    public SongResponse update(Long id, SongRequest request) {
        Song song = getSong(id);
        Album album = getAlbum(request.getAlbumId());

        song.setTitle(request.getTitle().trim());
        song.setDurationSeconds(request.getDurationSeconds());
        song.setGenres(getGenres(request.getGenreIds()));
        if (!song.getAlbum().getId().equals(album.getId())) {
            song.getAlbum().getSongs().remove(song);
            song.setAlbum(album);
            album.getSongs().add(song);
        }
        return SongResponse.from(song);
    }

    /**
     * Deletes a song: removes it from playlists and likes, deletes the
     * database row and finally the audio file.
     *
     * @param id song id
     */
    @Transactional
    public void delete(Long id) {
        Song song = getSong(id);
        String audioFile = song.getAudioFileName();

        songRepository.removeFromAllPlaylists(List.of(id));
        songRepository.removeFromAllLikes(List.of(id));
        song.getAlbum().getSongs().remove(song);
        songRepository.delete(song);

        fileStorageService.deleteAudio(audioFile);
    }

    /**
     * Counts one play using an atomic UPDATE query.
     *
     * @param id song id
     * @return the new play count
     */
    @Transactional
    public long registerPlay(Long id) {
        if (songRepository.incrementPlayCount(id) == 0) {
            throw new ResourceNotFoundException("Song", id);
        }
        return songRepository.findById(id)
                .map(Song::getPlayCount)
                .orElse(0L);
    }

    /**
     * Loads the audio file of a song for streaming.
     *
     * @param id song id
     * @return file resource and its MIME type
     */
    @Transactional(readOnly = true)
    public SongAudio loadAudio(Long id) {
        Song song = getSong(id);
        Resource resource =
                fileStorageService.loadAudio(song.getAudioFileName());
        String contentType =
                fileStorageService.audioContentType(song.getAudioFileName());
        return new SongAudio(resource, contentType);
    }

    private Song getSong(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));
    }

    private Album getAlbum(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album", id));
    }

    private Set<Genre> getGenres(Set<Long> genreIds) {
        Set<Genre> genres = new HashSet<>();
        if (genreIds == null) {
            return genres;
        }

        for (Long genreId : genreIds) {
            genres.add(genreRepository.findById(genreId).orElseThrow(
                    () -> new ResourceNotFoundException("Genre", genreId)));
        }
        return genres;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim();
    }

    /**
     * Audio file of a song together with its MIME type.
     *
     * @param resource    the file
     * @param contentType MIME type such as audio/mpeg
     */
    public record SongAudio(Resource resource, String contentType) {
    }
}
