/*
 * WebExceptionHandler.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Shows friendly error pages for the Thymeleaf website.
 */
package ge.melodia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Error handling for the HTML pages (classes annotated with
 * {@code @Controller}). REST controllers are handled first by
 * {@link ApiExceptionHandler}.
 */
@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException e) {
        return errorPage(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DuplicateResourceException.class,
            InvalidFileException.class})
    public ModelAndView handleBadRequest(RuntimeException e) {
        return errorPage(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleTooLarge(MaxUploadSizeExceededException e) {
        return errorPage(HttpStatus.PAYLOAD_TOO_LARGE,
                "The file is too large. The maximum size is 20 MB.");
    }

    private ModelAndView errorPage(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", message);
        return mav;
    }
}
