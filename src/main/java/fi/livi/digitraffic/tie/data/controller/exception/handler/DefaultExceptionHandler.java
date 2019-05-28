package fi.livi.digitraffic.tie.data.controller.exception.handler;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.xml.bind.MarshalException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.ServletWebRequest;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.service.BadRequestException;

@ControllerAdvice
public class DefaultExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(final TypeMismatchException exception, final ServletWebRequest request) {
        final String parameterValue = exception.getValue().toString();
        final String requiredType = exception.getRequiredType().getSimpleName();

        return getErrorResponseEntityAndLogError(
            request,
            String.format("Query parameter type mismatch. queryString=%s, parameterValue=%s, expectedType=%s",
                          request.getRequest().getQueryString(), requiredType, parameterValue),
            HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(final MissingServletRequestParameterException exception, final ServletWebRequest request) {
        final String parameterName = exception.getParameterName();
        final String requiredType = exception.getParameterType();

        return getErrorResponseEntityAndLogError(
            request,
            String.format("Query parameter missing. queryString=%s, parameterName=%s, expectedType=%S",
                          request.getRequest().getQueryString(), parameterName, requiredType),
            HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(final ConstraintViolationException exception, final ServletWebRequest request) {
        final String message = exception.getConstraintViolations().stream().map(v -> getViolationMessage(v)).collect(Collectors.joining
            (","));

        return getErrorResponseEntityAndLogError(
            request,
            String.format("Constraint violation. queryString=%s, errorMessage=%s",
                          request.getRequest().getQueryString(), message),
            HttpStatus.BAD_REQUEST,
            exception);
    }

    @ExceptionHandler({ ObjectNotFoundException.class, ResourceAccessException.class, BadRequestException.class })
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleObjectNotFoundException(final Exception exception, final ServletWebRequest request) {
        final HttpStatus status;
        if (exception instanceof ObjectNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (exception instanceof BadRequestException) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return getErrorResponseEntityAndLogError(request, exception.getMessage(), status, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogError(request, "Media type not acceptable", HttpStatus.NOT_ACCEPTABLE, exception);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleAbortedConnection(final IOException exception, final ServletWebRequest request)
    {
        // avoids compile/runtime dependency by using class name
        if (isClientAbortException(exception)) {
            log.warn("500 Internal Server Error: exceptionClass={}", exception.getClass().getName());
            return null;
        }

        return getErrorResponseEntityAndLogError(request, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpMessageNotWritableException(final Exception exception, final ServletWebRequest request) {

        if (exception.getCause() != null && exception.getCause() instanceof javax.xml.bind.MarshalException) {
            final MarshalException cause = (MarshalException) exception.getCause();

            if ( isClientAbortException(cause.getLinkedException()) ) {
                log.warn("500 Internal Server Error: exceptionClass={} exceptionMessage={}", exception.getClass().getName(), exception.getMessage());
                return null;
            }
        }

        return getErrorResponseEntityAndLogError(request, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogError(request, exception.getMessage(), HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogError(request, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException exception, final ServletWebRequest request) {
        final String errorMsg = String.format("Illegal %s: %s. Supported types: %s",
                                              HttpHeaders.CONTENT_TYPE, request.getHeader(HttpHeaders.CONTENT_TYPE), exception.getSupportedMediaTypes());
        return getErrorResponseEntityAndLogError(request, errorMsg, HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogError(request, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ResponseEntity<ErrorResponse> getErrorResponseEntityAndLogError(final ServletWebRequest request,
                                                                            final String errorMsg,
                                                                            final HttpStatus httpStatus,
                                                                            final Exception exception) {
        log.error(String.format("httpStatus=%s reasonPhrase=%s requestURI=%s errorMessage=%s",
                                httpStatus.value(), httpStatus.getReasonPhrase(),
                                request.getRequest().getRequestURI(), errorMsg), exception);
        return new ResponseEntity<>(new ErrorResponse(Timestamp.from(ZonedDateTime.now().toInstant()),
                                    httpStatus.value(),
                                    httpStatus.getReasonPhrase(),
                                    errorMsg,
                                    request.getRequest().getRequestURI()),
                                    httpStatus);
    }

    private static String getViolationMessage(final ConstraintViolation<?> violation) {
        final Path.Node paramPath = Iterables.getLast(violation.getPropertyPath());
        final String paramName = paramPath != null ? paramPath.toString() : null;
        return String.format("violatingParameter=%s, parameterValue=%s, violationMessage=%s %s",
                             paramName, violation.getInvalidValue(),
                             paramName, violation.getMessage());
    }

    private boolean isClientAbortException(final Throwable exception) {
        return exception != null && exception.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException");
    }
}
