package io.bytegate;


import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;

public interface RouteHandler {
    HttpResponse handle(HttpRequest request);
}
