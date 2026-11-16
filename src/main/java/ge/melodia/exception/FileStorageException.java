/*
 * FileStorageException.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Thrown when a file cannot be written to or read from disk.
 */
package ge.melodia.exception;

/**
 * Results in HTTP 500 Internal Server Error.
 */
public class FileStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
