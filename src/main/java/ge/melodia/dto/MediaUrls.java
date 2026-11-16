/*
 * MediaUrls.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Helpers that build public URLs for stored media files.
 */
package ge.melodia.dto;

/**
 * Builds the URLs under which uploaded files are served.
 */
public final class MediaUrls {

    public static final String IMAGE_URL_PREFIX = "/media/images/";

    private MediaUrls() {
    }

    public static String image(String fileName) {
        if (fileName == null) {
            return null;
        }
        return IMAGE_URL_PREFIX + fileName;
    }

    public static String stream(Long songId) {
        return "/api/songs/" + songId + "/stream";
    }

    public static String formatDuration(int totalSeconds) {
        return String.format("%d:%02d", totalSeconds / 60,
                totalSeconds % 60);
    }
}
