package fi.livi.digitraffic.tie.data.controller.exception.handler;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.ServletWebRequest;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;

@ControllerAdvice
public class DefaultExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(final TypeMismatchException exception, final ServletWebRequest request) {
        final String parameterValue = exception.getValue().toString();
        final String requiredType = exception.getRequiredType().getSimpleName();

        log.info("Query parameter type mismatch. Uri: {}, query string: {}, required type: {}",
                 request.getRequest().getRequestURI(), request.getRequest().getQueryString(), requiredType);

        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                                                      HttpStatus.BAD_REQUEST.value(),
                                                      HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                      String.format("Invalid format for parameter. Target type: %s, parameter: %s",
                                                                    requiredType, parameterValue),
                                                      request.getRequest().getRequestURI()),
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(final MissingServletRequestParameterException exception, final ServletWebRequest request) {
        final String parameterName = exception.getParameterName();
        final String requiredType = exception.getParameterType();

        log.info("Query parameter missing. Uri: {}, query string: {}, required name: {], required type: {}",
                request.getRequest().getRequestURI(), request.getRequest().getQueryString(), parameterName, requiredType);

        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                String.format("Missing parameter. Target type: %s, parameter: %s",
                        requiredType, parameterName),
                request.getRequest().getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(final ConstraintViolationException exception, final ServletWebRequest request) {

        String message = exception.getConstraintViolations().stream().map(v -> getViolationMessage(v)).collect(Collectors.joining(", "));

        log.info("Constraint violation. Uri: {}, query string: {}, violations: {}",
                 request.getRequest().getRequestURI(), request.getRequest().getQueryString(), message);

        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                                                      HttpStatus.BAD_REQUEST.value(),
                                                      HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                      message,
                                                      request.getRequest().getRequestURI()),
                                    HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ ObjectNotFoundException.class, ResourceAccessException.class })
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleObjectNotFoundException(final Exception exception, final ServletWebRequest request) {

        HttpStatus status;
        if (exception instanceof ObjectNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                                                      status.value(),
                                                      status.getReasonPhrase(),
                                                      exception.getMessage(),
                                                      request.getRequest().getRequestURI()),
                                    status);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(final Exception exception, final ServletWebRequest request) {
        log.info(HttpStatus.NOT_ACCEPTABLE.value() + " " + HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(), exception);

        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                HttpStatus.NOT_ACCEPTABLE.value(),
                HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(),
                "Media type not acceptable",
                request.getRequest().getRequestURI()),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleClientAbortException(final Exception exception, final ServletWebRequest request) {
        log.warn("{} {} ({})",HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), exception.getClass().getName());
        // Return null because connection is closed and it's impossible to return anything to client.
        // If something is returned it will cause another exception and that we don't want that to happen.
        return null;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(final Exception exception, final ServletWebRequest request) {
        log.error(HttpStatus.INTERNAL_SERVER_ERROR.value() + " " + HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), exception);
        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                                                      HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                      HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                      "Unknown error",
                                                      request.getRequest().getRequestURI()),
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static String getViolationMessage(final ConstraintViolation<?> violation) {
        return String.format("Violation: %s = %s - %s", Iterables.getLast(violation.getPropertyPath()), violation.getInvalidValue(),
                             violation.getMessage());
    }
}
