/*
 * ResourceNotFoundException.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Thrown when a requested record does not exist.
 */
package ge.melodia.exception;

/**
 * Results in HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " with id " + id + " was not found");
    }
}
