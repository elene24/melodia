/*
 * GenreStatsView.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Projection for the native genre statistics query.
 */
package ge.melodia.dto;

/**
 * Interface-based projection filled by a native SQL query.
 */
public interface GenreStatsView {

    Long getId();

    String getName();

    Long getSongCount();
}
