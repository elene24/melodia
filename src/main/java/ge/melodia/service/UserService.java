/*
 * UserService.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Business logic for users, likes and follows.
 */
package ge.melodia.service;

import java.util.List;

import ge.melodia.dto.SongResponse;
import ge.melodia.dto.UserRequest;
import ge.melodia.dto.UserResponse;
import ge.melodia.entity.Artist;
import ge.melodia.entity.Song;
import ge.melodia.entity.User;
import ge.melodia.exception.DuplicateResourceException;
import ge.melodia.exception.ResourceNotFoundException;
import ge.melodia.repository.ArtistRepository;
import ge.melodia.repository.SongRepository;
import ge.melodia.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages users, their liked songs and followed artists.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;

    public UserService(UserRepository userRepository,
            SongRepository songRepository,
            ArtistRepository artistRepository) {
        this.userRepository = userRepository;
        this.songRepository = songRepository;
        this.artistRepository = artistRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAllForSelect() {
        return userRepository.findAllByOrderByDisplayNameAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("Username '"
                    + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email '"
                    + request.getEmail() + "' is already registered");
        }

        User user = new User(request.getUsername(), request.getEmail(),
                request.getDisplayName());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = getUser(id);
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(
                request.getUsername(), id)) {
            throw new DuplicateResourceException("Username '"
                    + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(
                request.getEmail(), id)) {
            throw new DuplicateResourceException("Email '"
                    + request.getEmail() + "' is already registered");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        return UserResponse.from(user);
    }

    /**
     * Deletes a user. Playlists are removed by cascade; the rows in the
     * like and follow join tables are removed by Hibernate because the
     * user owns those relationships.
     *
     * @param id user id
     */
    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUser(id));
    }

    @Transactional(readOnly = true)
    public Page<SongResponse> findLikedSongs(Long userId, Pageable pageable) {
        getUser(userId);
        return songRepository.findLikedByUser(userId, pageable)
                .map(SongResponse::from);
    }

    @Transactional(readOnly = true)
    public List<SongResponse> findRecommendations(Long userId, int limit) {
        getUser(userId);
        return songRepository
                .findRecommendedForUser(userId, PageRequest.of(0, limit))
                .stream()
                .map(SongResponse::from)
                .toList();
    }

    /**
     * Likes a song. Liking a song twice has no effect.
     *
     * @param userId user id
     * @param songId song id
     * @return the updated user
     */
    @Transactional
    public UserResponse likeSong(Long userId, Long songId) {
        User user = getUser(userId);
        user.getLikedSongs().add(getSong(songId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse unlikeSong(Long userId, Long songId) {
        User user = getUser(userId);
        user.getLikedSongs().remove(getSong(songId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse followArtist(Long userId, Long artistId) {
        User user = getUser(userId);
        user.getFollowedArtists().add(getArtist(artistId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse unfollowArtist(Long userId, Long artistId) {
        User user = getUser(userId);
        user.getFollowedArtists().remove(getArtist(artistId));
        return UserResponse.from(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Song getSong(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song", id));
    }

    private Artist getArtist(Long id) {
        return artistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist", id));
    }
}
