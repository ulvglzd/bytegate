package io.bytegate;

import io.bytegate.log.Logger;
import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;
import io.bytegate.util.HttpRequestParser;
import io.bytegate.util.HttpResponseWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class CoreRequestHandler {

    private final RequestRouter router;
    private final Logger logger;

    public CoreRequestHandler(RequestRouter router, Logger logger) {
        this.router = router;
        this.logger = logger;
    }

    public void handleRequest(Socket conn) {
        HttpRequest request = parseRequest(conn);
        logRequest(request);

        RouteMatch match = router.resolve(request.getMethod(), request.getPath());

        setRequestPathVariables(match, request);

        HttpResponse response = getResponse(match, request);

        writeToOutputStream(conn, response);
    }

    private void setRequestPathVariables(RouteMatch match, HttpRequest request) {
        if (match != null && !match.pathParams().isEmpty()) {
            request.setPathParams(match.pathParams());
            logger.debug("Path params: " + match.pathParams());
        }
    }

    private void writeToOutputStream(Socket conn, HttpResponse response) {
        try {
            HttpResponseWriter.write(conn.getOutputStream(), response);
        } catch (IOException e) {
            logger.error("Failed to write response: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private HttpResponse getResponse(RouteMatch match, HttpRequest request) {
        HttpResponse response;
        if (match == null) {
            response = HttpResponse.notFound("Path not found with method: " + request.getMethod() + " " + request.getPath());
        } else {
            response = match.handler().handle(request);
        }
        logger.info(request.getMethod() + " " + request.getPath() + " -> " + response.getStatusCode());
        return response;
    }

    private void logRequest(HttpRequest request) {
        logger.info(request.getMethod() + " " + request.getUri());

        logger.debug("Headers: " + formatHeaders(request.getHeaders()));
        if (!request.getQueryParams().isEmpty()) {
            logger.debug("Query params: " + request.getQueryParams());
        }
        request.bodyPublisher().ifPresent(bp -> logger.debug("Body: " + bp));
    }

    private HttpRequest parseRequest(Socket conn) {
        HttpRequest request;
        try {
            request = HttpRequestParser.parse(conn.getInputStream());
        } catch (IOException e) {
            logger.error("Failed to parse request: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return request;
    }

    private String formatHeaders(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder("{");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append(", "));
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return sb.toString();
    }
}
