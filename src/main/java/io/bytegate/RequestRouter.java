package io.bytegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RequestRouter {

    private final Map<String, RouteHandler> exactRoutes = new HashMap<>();
    private final List<ParameterizedRoute> parameterizedRoutes = new ArrayList<>();

    public void register(String method, String path, RouteHandler handler) {
        String key = method + ":" + path;
        if (path.contains("{")) {
            parameterizedRoutes.add(new ParameterizedRoute(key, path.split("/"), handler));
        } else {
            exactRoutes.put(key, handler);
        }
    }

    public RouteMatch resolve(String method, String path) {
        RouteHandler exact = exactRoutes.get(method + ":" + path);
        if (exact != null) {
            return new RouteMatch(exact, Collections.emptyMap());
        }

        String[] requestSegments = path.split("/");
        for (ParameterizedRoute route : parameterizedRoutes) {
            if (!route.key.startsWith(method + ":")) {
                continue;
            }

            Map<String, String> params = match(route.segments, requestSegments);
            if (params != null) {
                return new RouteMatch(route.handler, params);
            }
        }

        return null;
    }

    private Map<String, String> match(String[] patternSegments, String[] requestSegments) {
        if (patternSegments.length != requestSegments.length) {
            return null;
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < patternSegments.length; i++) {
            String pattern = patternSegments[i];
            String actual = requestSegments[i];

            if (pattern.startsWith("{") && pattern.endsWith("}")) {
                String paramName = pattern.substring(1, pattern.length() - 1);
                params.put(paramName, actual);
            } else if (!pattern.equals(actual)) {
                return null;
            }
        }
        return params;
    }

    private record ParameterizedRoute(String key, String[] segments, RouteHandler handler) {
    }
}
