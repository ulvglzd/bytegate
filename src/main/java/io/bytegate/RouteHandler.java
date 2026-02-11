package io.bytegate;


import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;

@FunctionalInterface
public interface RouteHandler {
    HttpResponse handle(HttpRequest request);
}
