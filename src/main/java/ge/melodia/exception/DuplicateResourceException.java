/*
 * DuplicateResourceException.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Thrown when a record would violate a uniqueness rule.
 */
package ge.melodia.exception;

/**
 * Results in HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}
