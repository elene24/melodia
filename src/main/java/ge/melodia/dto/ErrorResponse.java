/*
 * ErrorResponse.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Error body returned by the REST API.
 */
package ge.melodia.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform JSON error body.
 *
 * @param timestamp   when the error happened
 * @param status      HTTP status code
 * @param error       HTTP status reason
 * @param message     human readable message
 * @param path        request path
 * @param fieldErrors validation errors per field (may be empty)
 */
public record ErrorResponse(LocalDateTime timestamp, int status,
        String error, String message, String path,
        Map<String, String> fieldErrors) {
}
