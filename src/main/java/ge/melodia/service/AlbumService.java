/*
 * AlbumService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Business logic for albums.
 */
package ge.melodia.service;

import java.util.List;

import ge.melodia.dto.AlbumRequest;
import ge.melodia.dto.AlbumResponse;
import ge.melodia.dto.SongResponse;
import ge.melodia.entity.Album;
import ge.melodia.entity.Artist;
import ge.melodia.entity.Song;
import ge.melodia.exception.ResourceNotFoundException;
import ge.melodia.repository.AlbumRepository;
import ge.melodia.repository.ArtistRepository;
import ge.melodia.repository.SongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Creates, updates, deletes and lists albums.
 */
@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final FileStorageService fileStorageService;

    public AlbumService(AlbumRepository albumRepository,
            ArtistRepository artistRepository, SongRepository songRepository,
            FileStorageService fileStorageService) {
        this.albumRepository = albumRepository;
        this.artistRepository = artistRepository;
        this.songRepository = songRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public Page<AlbumResponse> search(String title, Pageable pageable) {
        String text = title == null ? "" : title.trim();
        return albumRepository.findByTitleContainingIgnoreCase(text, pageable)
                .map(AlbumResponse::from);
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> findLatest() {
        return albumRepository.findTop6ByOrderByReleaseDateDesc().stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> findAllForSelect() {
        return albumRepository.findAllWithArtist().stream()
                .map(AlbumResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumResponse findById(Long id) {
        return AlbumResponse.from(getAlbum(id));
    }

    @Transactional(readOnly = true)
    public List<SongResponse> findSongs(Long albumId) {
        getAlbum(albumId);
        return songRepository.findByAlbumIdOrderByIdAsc(albumId).stream()
                .map(SongResponse::from)
                .toList();
    }

    @Transactional
    public AlbumResponse create(AlbumRequest request) {
        Artist artist = getArtist(request.getArtistId());
        Album album = new Album(request.getTitle().trim(),
                request.getReleaseDate(), artist);
        artist.getAlbums().add(album);
        return AlbumResponse.from(albumRepository.save(album));
    }

    /**
     * Creates an album and stores the optional cover in one transaction.
     *
     * @param request album data
     * @param cover   optional cover image (may be empty)
     * @return the created album
     */
    @Transactional
    public AlbumResponse create(AlbumRequest request, MultipartFile cover) {
        AlbumResponse created = create(request);
        if (cover != null && !cover.isEmpty()) {
            return uploadCover(created.id(), cover);
        }
        return created;
    }

    @Transactional
    public AlbumResponse update(Long id, AlbumRequest request) {
        Album album = getAlbum(id);
        Artist artist = getArtist(request.getArtistId());

        album.setTitle(request.getTitle().trim());
        album.setReleaseDate(request.getReleaseDate());
        album.setArtist(artist);
        return AlbumResponse.from(album);
    }

    @Transactional
    public AlbumResponse uploadCover(Long id, MultipartFile cover) {
        Album album = getAlbum(id);
        String oldFile = album.getCoverFileName();

        album.setCoverFileName(fileStorageService.storeImage(cover));
        fileStorageService.deleteImage(oldFile);
        return AlbumResponse.from(album);
    }

    /**
     * Deletes an album and its songs (and their files).
     *
     * @param id album id
     */
    @Transactional
    public void delete(Long id) {
        Album album = getAlbum(id);
        List<Long> songIds = album.getSongs().stream()
                .map(Song::getId)
                .toList();
        List<String> audioFiles = album.getSongs().stream()
                .map(Song::getAudioFileName)
                .toList();
        String coverFile = album.getCoverFileName();

        if (!songIds.isEmpty()) {
            songRepository.removeFromAllPlaylists(songIds);
            songRepository.removeFromAllLikes(songIds);
        }
        album.getArtist().getAlbums().remove(album);
        albumRepository.delete(album);

        audioFiles.forEach(fileStorageService::deleteAudio);
        fileStorageService.deleteImage(coverFile);
    }

    private Album getAlbum(Long id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Album", id));
    }

    private Artist getArtist(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
    }
}
