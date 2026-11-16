/*
 * PlaylistService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Business logic for playlists.
 */
package ge.melodia.service;

import java.time.LocalDateTime;
import java.util.List;

import ge.melodia.dto.PlaylistRequest;
import ge.melodia.dto.PlaylistResponse;
import ge.melodia.dto.PlaylistSummaryResponse;
import ge.melodia.entity.Playlist;
import ge.melodia.entity.Song;
import ge.melodia.entity.User;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.exception.ResourceNotFoundException;
import ge.melodia.repository.PlaylistRepository;
import ge.melodia.repository.SongRepository;
import ge.melodia.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates playlists and adds or removes their songs.
 */
@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongRepository songRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
            UserRepository userRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songRepository = songRepository;
    }

    @Transactional(readOnly = true)
    public Page<PlaylistSummaryResponse> search(String name,
            Pageable pageable) {
        String text = name == null ? "" : name.trim();
        return playlistRepository.findByNameContainingIgnoreCase(text,
                pageable).map(PlaylistSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public List<PlaylistSummaryResponse> findByOwner(Long userId) {
        getUser(userId);
        return playlistRepository.findByOwnerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PlaylistSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlaylistSummaryResponse> findAllForSelect() {
        return playlistRepository.findAll().stream()
                .map(PlaylistSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaylistResponse findById(Long id) {
        return toResponse(getPlaylist(id));
    }

    @Transactional
    public PlaylistResponse create(Long userId, PlaylistRequest request) {
        User owner = getUser(userId);
        String name = request.getName().trim();
        if (playlistRepository.existsByOwnerIdAndNameIgnoreCase(userId,
                name)) {
            throw new DuplicateResourceException(
                    "You already have a playlist called '" + name + "'");
        }

        Playlist playlist = new Playlist(name, request.getDescription(),
                owner);
        owner.getPlaylists().add(playlist);
        return toResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistResponse update(Long id, PlaylistRequest request) {
        Playlist playlist = getPlaylist(id);
        String name = request.getName().trim();
        if (!playlist.getName().equalsIgnoreCase(name)
                && playlistRepository.existsByOwnerIdAndNameIgnoreCase(
                        playlist.getOwner().getId(), name)) {
            throw new DuplicateResourceException(
                    "You already have a playlist called '" + name + "'");
        }

        playlist.setName(name);
        playlist.setDescription(request.getDescription());
        return toResponse(playlistRepository.saveAndFlush(playlist));
    }

    @Transactional
    public void delete(Long id) {
        Playlist playlist = getPlaylist(id);
        playlist.getOwner().getPlaylists().remove(playlist);
        playlistRepository.delete(playlist);
    }

    /**
     * Adds a song to a playlist. Loading both records, checking for a
     * duplicate and writing the join-table row happen in one
     * transaction: either everything succeeds or nothing is changed.
     *
     * @param playlistId playlist id
     * @param songId     song id
     * @return the updated playlist
     */
    @Transactional
    public PlaylistResponse addSong(Long playlistId, Long songId) {
        Playlist playlist = getPlaylist(playlistId);
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song",
                        songId));

        if (!playlist.getSongs().add(song)) {
            throw new DuplicateResourceException("'" + song.getTitle()
                    + "' is already in this playlist");
        }
        playlist.setUpdatedAt(LocalDateTime.now());
        return toResponse(playlistRepository.saveAndFlush(playlist));
    }

    /**
     * Removes a song from a playlist.
     *
     * @param playlistId playlist id
     * @param songId     song id
     * @return the updated playlist
     */
    @Transactional
    public PlaylistResponse removeSong(Long playlistId, Long songId) {
        Playlist playlist = getPlaylist(playlistId);
        boolean removed = playlist.getSongs()
                .removeIf(song -> song.getId().equals(songId));

        if (!removed) {
            throw new ResourceNotFoundException("Song in playlist", songId);
        }
        playlist.setUpdatedAt(LocalDateTime.now());
        return toResponse(playlistRepository.saveAndFlush(playlist));
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        Long total = playlist.getId() == null ? 0L
                : playlistRepository.sumDurationSeconds(playlist.getId());
        return PlaylistResponse.from(playlist, total == null ? 0L : total);
    }

    private Playlist getPlaylist(Long id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Playlist",
                        id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
