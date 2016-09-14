package fi.livi.digitraffic.tie.data.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(Class<?> entityClass, Object identifier) {
        this(entityClass.getSimpleName(), identifier);
    }

    public ObjectNotFoundException(String objectName, Object identifier) {
        super("Object of [" + objectName + "] with identifier [" + identifier + "]: not found");
    }
}
