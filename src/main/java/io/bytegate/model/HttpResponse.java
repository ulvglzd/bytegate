package io.bytegate.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final String body;

    private HttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.reasonPhrase = builder.reasonPhrase;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));
        this.body = builder.body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public static HttpResponse ok(String body) {
        return new Builder()
                .status(200, "OK")
                .header("Content-Type", "text/plain")
                .body(body)
                .build();
    }

    public static HttpResponse notFound() {
        return new Builder()
                .status(404, "Not Found")
                .header("Content-Type", "text/plain")
                .body("404 Not Found")
                .build();
    }

    public static HttpResponse notFound(String message) {
        return new Builder()
                .status(404, "Not Found")
                .header("Content-Type", "text/plain")
                .body(message)
                .build();
    }

    public static HttpResponse badRequest(String message) {
        return new Builder()
                .status(400, "Bad Request")
                .header("Content-Type", "text/plain")
                .body(message)
                .build();
    }

    public static HttpResponse serviceUnavailable() {
        return new Builder()
                .status(503, "Service Unavailable")
                .header("Content-Type", "text/plain")
                .body("503 Service Unavailable")
                .build();
    }

    public static HttpResponse internalServerError() {
        return new Builder()
                .status(500, "Internal Server Error")
                .header("Content-Type", "text/plain")
                .body("500 Internal Server Error")
                .build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int statusCode = 200;
        private String reasonPhrase = "OK";
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String body = "";

        public Builder status(int statusCode, String reasonPhrase) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }
}
