/*
 * Song.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * A single track with an uploaded audio file.
 */
package ge.melodia.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A song belongs to one album (many-to-one) and has many genres
 * (many-to-many). The audio file is stored on disk and only its file
 * name is kept in the database.
 */
@Entity
@Table(name = "songs")
@Getter
@Setter
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    /** Name of the stored audio file inside the audio folder. */
    @Column(name = "audio_file_name", nullable = false)
    private String audioFileName;

    /** File name as it was on the uploader's computer. */
    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "play_count", nullable = false)
    private long playCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToMany
    @JoinTable(name = "song_genres",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
