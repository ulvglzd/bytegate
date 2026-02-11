package io.bytegate;

import io.bytegate.annotation.Route;
import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;
import java.lang.reflect.Method;

public class ControllerScanner {

    public static void scan(Object controller, RequestRouter router) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            Route route = method.getAnnotation(Route.class);
            if (route == null) {
                continue;
            }

            validateMethod(method);

            method.setAccessible(true);
            RouteHandler handler = createHandler(controller, method);
            router.register(route.method(), route.path(), handler);
        }
    }

    private static RouteHandler createHandler(Object controller, Method method) {
        return request -> {
            try {
                return (HttpResponse) method.invoke(controller, request);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke @Route method: " + method.getName(), e);
            }
        };
    }

    private static void validateMethod(Method method) {
        if (method.getParameterCount() != 1
                || method.getParameterTypes()[0] != HttpRequest.class
                || method.getReturnType() != HttpResponse.class) {
            throw new IllegalArgumentException(
                    "@Route method must have signature: HttpResponse methodName(HttpRequest). "
                            + "Invalid method: " + method.getName());
        }
    }
}
