/*
 * ApiExceptionHandler.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Turns exceptions into JSON error responses for the REST API.
 */
package ge.melodia.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import ge.melodia.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation
        .MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support
        .MissingServletRequestPartException;

/**
 * Global error handling for classes annotated with
 * {@code @RestController}.
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException e, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), request,
                Map.of());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateResourceException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(
            InvalidFileException e, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), request,
                Map.of());
    }

    /**
     * Validation errors of {@code @Valid @RequestBody} and
     * {@code @Valid @ModelAttribute} parameters.
     *
     * @param e       exception with the field errors
     * @param request current request
     * @return 400 response listing every invalid field
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleValidation(BindException e,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(),
                    error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request,
                fieldErrors);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request: "
                + e.getMessage(), request, Map.of());
    }

    /**
     * Thrown when the "sort" parameter names a field that does not
     * exist, for example {@code ?sort=nonsense,asc}.
     *
     * @param e       the exception
     * @param request current request
     * @return 400 response
     */
    @ExceptionHandler({PropertyReferenceException.class,
            InvalidDataAccessApiUsageException.class})
    public ResponseEntity<ErrorResponse> handleBadSort(Exception e,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid sort or filter parameter", request, Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE,
                "File is too large (maximum 20 MB)", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(
            DataIntegrityViolationException e, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT,
                "The operation conflicts with existing data", request,
                Map.of());
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(
            FileStorageException e, HttpServletRequest request) {
        LOG.error("File storage error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),
                request, Map.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
            String message, HttpServletRequest request,
            Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(),
                status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
