/*
 * ArtistService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Business logic for artists.
 */
package ge.melodia.service;

import java.util.ArrayList;
import java.util.List;

import ge.melodia.dto.AlbumResponse;
import ge.melodia.dto.ArtistRequest;
import ge.melodia.dto.ArtistResponse;
import ge.melodia.entity.Album;
import ge.melodia.entity.Artist;
import ge.melodia.entity.Song;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.exception.ResourceNotFoundException;
import ge.melodia.repository.AlbumRepository;
import ge.melodia.repository.ArtistRepository;
import ge.melodia.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Creates, updates, deletes and searches artists.
 */
@Service
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final FileStorageService fileStorageService;

    public ArtistService(ArtistRepository artistRepository,
            AlbumRepository albumRepository, SongRepository songRepository,
            FileStorageService fileStorageService) {
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.songRepository = songRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Returns one page of artists whose name contains the given text.
     *
     * @param name     search text, may be null
     * @param pageable page number, size and sort
     * @return one page of artists
     */
    @Transactional(readOnly = true)
    public Page<ArtistResponse> search(String name, Pageable pageable) {
        String text = name == null ? "" : name.trim();
        return artistRepository.findByNameContainingIgnoreCase(text, pageable)
                .map(ArtistResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> findAllForSelect() {
        return artistRepository.findAllByOrderByNameAsc().stream()
                .map(ArtistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ArtistResponse> findMostFollowed(int limit) {
        return artistRepository.findMostFollowed(PageRequest.of(0, limit))
                .stream()
                .map(ArtistResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArtistResponse findById(Long id) {
        return ArtistResponse.from(getArtist(id));
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> findAlbums(Long artistId) {
        getArtist(artistId);
        return albumRepository.findByArtistIdOrderByReleaseDateDesc(artistId)
                .stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @Transactional
    public ArtistResponse create(ArtistRequest request) {
        String name = request.getName().trim();
        if (artistRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException(
                    "Artist '" + name + "' already exists");
        }
        Artist artist = new Artist(name, request.getBio());
        return ArtistResponse.from(artistRepository.save(artist));
    }

    /**
     * Creates an artist and stores the optional photo in one
     * transaction: if the photo is rejected, the artist is not saved.
     *
     * @param request artist data
     * @param image   optional photo (may be empty)
     * @return the created artist
     */
    @Transactional
    public ArtistResponse create(ArtistRequest request, MultipartFile image) {
        ArtistResponse created = create(request);
        if (image != null && !image.isEmpty()) {
            return uploadImage(created.id(), image);
        }
        return created;
    }

    @Transactional
    public ArtistResponse update(Long id, ArtistRequest request) {
        Artist artist = getArtist(id);
        String name = request.getName().trim();
        if (artistRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException(
                    "Artist '" + name + "' already exists");
        }
        artist.setName(name);
        artist.setBio(request.getBio());
        return ArtistResponse.from(artist);
    }

    /**
     * Stores a new photo and removes the old one.
     *
     * @param id    artist id
     * @param image uploaded image
     * @return the updated artist
     */
    @Transactional
    public ArtistResponse uploadImage(Long id, MultipartFile image) {
        Artist artist = getArtist(id);
        String oldFile = artist.getImageFileName();

        artist.setImageFileName(fileStorageService.storeImage(image));
        fileStorageService.deleteImage(oldFile);
        return ArtistResponse.from(artist);
    }

    /**
     * Deletes an artist together with all albums and songs. Songs are
     * first removed from playlists and likes, then the files are
     * deleted from disk.
     *
     * @param id artist id
     */
    @Transactional
    public void delete(Long id) {
        Artist artist = getArtist(id);
        List<Long> songIds = new ArrayList<>();
        List<String> audioFiles = new ArrayList<>();
        List<String> imageFiles = new ArrayList<>();

        imageFiles.add(artist.getImageFileName());
        for (Album album : artist.getAlbums()) {
            imageFiles.add(album.getCoverFileName());
            for (Song song : album.getSongs()) {
                songIds.add(song.getId());
                audioFiles.add(song.getAudioFileName());
            }
        }

        if (!songIds.isEmpty()) {
            songRepository.removeFromAllPlaylists(songIds);
            songRepository.removeFromAllLikes(songIds);
        }
        artistRepository.deleteFollows(id);
        artistRepository.delete(artist);

        audioFiles.forEach(fileStorageService::deleteAudio);
        imageFiles.forEach(fileStorageService::deleteImage);
    }

    private Artist getArtist(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
    }
}
