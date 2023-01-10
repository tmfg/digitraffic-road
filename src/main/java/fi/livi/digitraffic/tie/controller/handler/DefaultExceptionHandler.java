package fi.livi.digitraffic.tie.controller.handler;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.xml.bind.MarshalException;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.google.common.collect.Iterables;

import fi.livi.digitraffic.tie.controller.DtMediaType;
import fi.livi.digitraffic.tie.helper.LoggerHelper;
import fi.livi.digitraffic.tie.service.BadRequestException;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

@ControllerAdvice
public class DefaultExceptionHandler {
    private final Logger logger;

    // log these exceptions with error
    private static final Set<Class<?>> errorLoggableExceptions = Set.of(
        NullPointerException.class,
        ResourceAccessException.class);

    // no need to log these exceptions at all
    private static final Set<Class<?>> nonLoggableExceptions = Set.of(
        ObjectNotFoundException.class,
        BadRequestException.class
    );

    public DefaultExceptionHandler(final Logger exceptionHandlerLogger) {
        this.logger = exceptionHandlerLogger;
    }

    @ExceptionHandler(TypeMismatchException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(final TypeMismatchException exception, final ServletWebRequest request) {
        final String parameterName = getExceptionPropertyName(exception);
        final String parameterValue = Objects.requireNonNullElse(exception.getValue(), "undefined").toString();
        final String requiredType = exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "undefined";

        return getErrorResponseEntityAndLogException(
            request,
            String.format("Query parameter type mismatch: queryString=%s, parameterName=%s parameterValue=%s, expectedType=%s",
                          request.getRequest().getQueryString(), parameterName, parameterValue, requiredType),
            HttpStatus.BAD_REQUEST, exception);
    }

    private String getExceptionPropertyName(final TypeMismatchException exception) {
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return ((MethodArgumentTypeMismatchException) exception).getName();
        }

        return exception.getPropertyName();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(final MissingServletRequestParameterException exception, final ServletWebRequest request) {
        final String parameterName = exception.getParameterName();
        final String requiredType = exception.getParameterType();

        return getErrorResponseEntityAndLogException(
            request,
            String.format("Query parameter missing: queryString=%s, parameterName=%s, expectedType=%s",
                          request.getRequest().getQueryString(), parameterName, requiredType),
            HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMissingPathVariableException(final MissingPathVariableException exception, final ServletWebRequest request) {
        final String pathVariableName = exception.getVariableName();
        final String parameterType = exception.getParameter().getParameterType().getSimpleName();

        return getErrorResponseEntityAndLogException(
            request,
            String.format("Path variable missing: pathVariableName: %s, expectedType=%s",
                          pathVariableName, parameterType),
            HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(final ConstraintViolationException exception, final ServletWebRequest request) {
        final String message = exception.getConstraintViolations().stream()
            .map(DefaultExceptionHandler::getViolationMessage)
            .collect(Collectors.joining(","));

        return getErrorResponseEntityAndLogException(
            request,
            String.format("Constraint violation. queryString=%s, errorMessage=%s",
                          request.getRequest().getQueryString(), message),
            HttpStatus.BAD_REQUEST,
            exception);
    }

    @ExceptionHandler({ ObjectNotFoundException.class, ResourceAccessException.class, BadRequestException.class, ConversionFailedException.class,
        IllegalArgumentException.class, MethodArgumentTypeMismatchException.class })
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleObjectNotFoundException(final Exception exception, final ServletWebRequest request) {
        final HttpStatus status;
        if (exception instanceof ObjectNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (exception instanceof BadRequestException || exception instanceof IllegalArgumentException || exception instanceof MethodArgumentTypeMismatchException || exception instanceof ConversionFailedException) {
            status = HttpStatus.BAD_REQUEST;
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return getErrorResponseEntityAndLogException(request, exception.getMessage(), status, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogException(request, "Media type not acceptable", HttpStatus.NOT_ACCEPTABLE, exception);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleAbortedConnection(final IOException exception, final ServletWebRequest request)
    {
        // avoids compile/runtime dependency by using class name
        if (isClientAbortException(exception)) {
            logger.warn("500 Internal Server Error: exceptionClass={}", exception.getClass().getName());
            return null;
        }

        return getErrorResponseEntityAndLogException(request, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpMessageNotWritableException(final Exception exception, final ServletWebRequest request) {
        if (exception.getCause() != null && exception.getCause() instanceof MarshalException) {
            final MarshalException cause = (MarshalException) exception.getCause();

            if ( isClientAbortException(cause.getLinkedException()) ) {
                logger.warn("500 Internal Server Error: exceptionClass={} exceptionMessage={}", exception.getClass().getName(),
                    exception.getMessage());
                return null;
            }
        }

        return getErrorResponseEntityAndLogException(request, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogException(request, exception.getMessage(), HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogException(request, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED, exception);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException exception, final ServletWebRequest request) {
        final String errorMsg = String.format("Illegal %s: %s. Supported types: %s",
                                              HttpHeaders.CONTENT_TYPE, request.getHeader(HttpHeaders.CONTENT_TYPE), exception.getSupportedMediaTypes());
        return getErrorResponseEntityAndLogException(request, errorMsg, HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(final Exception exception, final ServletWebRequest request) {
        return getErrorResponseEntityAndLogException(request, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ResponseEntity<ErrorResponse> getErrorResponseEntityAndLogException(final ServletWebRequest request,
                                                                                final String errorMsg,
                                                                                final HttpStatus httpStatus,
                                                                                final Exception exception) {
        // Remove a=b from errorMessage as it can contain values like "1971"-H"accept:application/json;charset=UTF-8"
        // and that will be indexed with key "1971"-H"accept:application/json;charset and value UTF-8"
        final String logMessage =
            String.format("httpStatus=%s reasonPhrase=%s requestURI=%s errorMessage: %s",
                httpStatus.value(), httpStatus.getReasonPhrase(), request.getRequest().getRequestURI(),
                LoggerHelper.objectToStringLoggerSafe(errorMsg));

        if(isErrorLoggableException(exception)) {
            logger.error(logMessage, exception);
        } else if(isInfoLoggableException(exception)) {
            logger.info(logMessage, exception);
        }

        return getErrorResponseEntity(httpStatus, errorMsg, request);
    }

    private ResponseEntity<ErrorResponse> getErrorResponseEntity(final HttpStatus httpStatus, final String errorMsg, final ServletWebRequest request) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(DtMediaType.APPLICATION_JSON);

        final ErrorResponse response = new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase(), errorMsg, request.getRequest().getRequestURI());

        return new ResponseEntity<>(response, headers, httpStatus);
    }


    private static String getViolationMessage(final ConstraintViolation<?> violation) {
        final Path.Node paramPath = Iterables.getLast(violation.getPropertyPath());
        final String paramName = paramPath != null ? paramPath.toString() : null;
        return String.format("violatingParameter=%s, parameterValue=%s, violationMessage=%s %s",
                             paramName, violation.getInvalidValue(),
                             paramName, violation.getMessage());
    }

    private static boolean isClientAbortException(final Throwable exception) {
        return exception instanceof ClientAbortException;
    }

    private static <TH extends Throwable> boolean isErrorLoggableException(final TH throwable) {
        return errorLoggableExceptions.stream().anyMatch(e -> e.isAssignableFrom(throwable.getClass()));
    }

    private static <TH extends Throwable> boolean isInfoLoggableException(final TH throwable) {
        return nonLoggableExceptions.stream().noneMatch(e -> e.isAssignableFrom(throwable.getClass()));
    }
}
