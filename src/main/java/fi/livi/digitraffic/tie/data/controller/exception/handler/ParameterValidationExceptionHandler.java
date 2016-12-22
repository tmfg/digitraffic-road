package fi.livi.digitraffic.tie.data.controller.exception.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import fi.livi.digitraffic.tie.data.controller.DataController;

@ControllerAdvice
public class ParameterValidationExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception, ServletWebRequest request) {
        log.error(HttpStatus.BAD_REQUEST.value() + " " + HttpStatus.BAD_REQUEST.getReasonPhrase(), exception);

        List<String> errors = new ArrayList<>();
        exception.getConstraintViolations().stream().forEach(violation -> {
            String paramName = resolveParamName(request, violation);
            errors.add(paramName + " " + violation.getMessage());
        });

        return buildResponseEntity(HttpStatus.BAD_REQUEST, errors, request, exception);
    }

    private ResponseEntity<Map<String, Object>> buildResponseEntity(HttpStatus httpStatus, List<String> errors, ServletWebRequest request, Exception exception) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", new Date());
        errorAttributes.put("status", httpStatus.value());
        errorAttributes.put("error", httpStatus.getReasonPhrase());
        errorAttributes.put("exception", exception.getClass().getSimpleName());
        errorAttributes.put("message", errors);
        errorAttributes.put("path", request.getRequest().getRequestURI());
        return new ResponseEntity<>(errorAttributes, httpStatus);
    }

    private String resolveParamName(WebRequest request, ConstraintViolation<?> violation) {
        try {
            Path.ParameterNode path = (Path.ParameterNode) Iterables.getLast(violation.getPropertyPath());
            int parameterIndex = path.getParameterIndex()-1;
            return Iterators.get(request.getParameterNames(), parameterIndex);
        } catch (Exception e) {
            return Iterables.getLast(violation.getPropertyPath()).getName();
        }
    }
}
