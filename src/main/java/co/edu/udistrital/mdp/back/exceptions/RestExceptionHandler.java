package co.edu.udistrital.mdp.back.exceptions;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Handles EntityNotFoundException. Created to encapsulate errors with more
     * detail than javax.persistence.EntityNotFoundException.
     *
     * @param ex the EntityNotFoundException
     * @return the ApiError object
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
            EntityNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    /**
     * Handles IllegalOperationException.
     *
     * @param ex the IllegalOperationException
     * @return the ApiError object
     */
    @ExceptionHandler(IllegalOperationException.class)
    protected ResponseEntity<Object> handleIllegalOperation(
            IllegalOperationException ex) {
        ApiError apiError = new ApiError(PRECONDITION_FAILED);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * Catch-all handler that logs the full stacktrace and returns a cleaned ApiError.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception caught by RestExceptionHandler", ex);
        ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR);
        apiError.setMessage(ex.getMessage() != null ? ex.getMessage() : "Internal Server Error");
        return buildResponseEntity(apiError);
    }

}