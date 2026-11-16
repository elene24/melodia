/*
 * Artist.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Musician or band that releases albums.
 */
package ge.melodia.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An artist owns many albums (one-to-many, bidirectional with Album).
 */
@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 2000)
    private String bio;

    /** Name of the uploaded photo inside the images folder. */
    @Column(name = "image_file_name")
    private String imageFileName;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL)
    @OrderBy("releaseDate DESC")
    private List<Album> albums = new ArrayList<>();

    public Artist(String name, String bio) {
        this.name = name;
        this.bio = bio;
    }
}
