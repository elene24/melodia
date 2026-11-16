/*
 * InvalidFileException.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Thrown when an uploaded file is missing or not allowed.
 */
package ge.melodia.exception;

/**
 * Results in HTTP 400 Bad Request.
 */
public class InvalidFileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidFileException(String message) {
        super(message);
    }
}
