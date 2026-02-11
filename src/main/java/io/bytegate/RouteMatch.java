package io.bytegate;

import java.util.Collections;
import java.util.Map;

public record RouteMatch(RouteHandler handler, Map<String, String> pathParams) {

    public RouteMatch(RouteHandler handler, Map<String, String> pathParams) {
        this.handler = handler;
        this.pathParams = Collections.unmodifiableMap(pathParams);
    }
}
