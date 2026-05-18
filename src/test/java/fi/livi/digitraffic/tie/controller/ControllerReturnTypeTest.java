package fi.livi.digitraffic.tie.controller;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fi.livi.digitraffic.tie.AbstractRestWebTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensures that no controller endpoint returns a plain String response.
 * All endpoints should return typed objects for proper OpenAPI documentation.
 */
class ControllerReturnTypeTest extends AbstractRestWebTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void noControllerMethodShouldReturnString() {
        final Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);

        final List<String> violations = new ArrayList<>();

        for (final Object controller : controllers.values()) {
            final Class<?> controllerClass = controller.getClass();
            // Skip CGLIB proxies – get the actual class
            final Class<?> targetClass = org.springframework.util.ClassUtils.getUserClass(controllerClass);

            for (final Method method : targetClass.getDeclaredMethods()) {
                if (!isRequestMapping(method)) {
                    continue;
                }

                final Type returnType = method.getGenericReturnType();

                if (returnsString(returnType)) {
                    violations.add(targetClass.getSimpleName() + "." + method.getName()
                            + " returns String type: " + returnType.getTypeName());
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Controller methods should not return String. Found violations:\n" + String.join("\n", violations));
    }

    private boolean isRequestMapping(final Method method) {
        return method.isAnnotationPresent(RequestMapping.class)
                || method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }

    private boolean returnsString(final Type type) {
        // Direct String return
        if (type == String.class) {
            return true;
        }

        // Generic types like ResponseEntity<String>, ResponseEntityWithLastModifiedHeader<String>
        if (type instanceof ParameterizedType parameterized) {
            final Type rawType = parameterized.getRawType();

            // Map<String, Object> is fine – String is just the key type
            if (rawType == Map.class || rawType == java.util.HashMap.class) {
                return false;
            }

            // For wrapper types (ResponseEntity, ResponseEntityWithLastModifiedHeader), check the body type argument
            for (final Type arg : parameterized.getActualTypeArguments()) {
                if (arg == String.class) {
                    return true;
                }
            }
        }

        return false;
    }
}
