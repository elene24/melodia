/*
 * Genre.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Music genre such as Pop, Rock or Jazz.
 */
package ge.melodia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A genre that songs can be tagged with (many-to-many with Song).
 */
@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Genre(String name) {
        this.name = name;
    }
}
