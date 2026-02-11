package io.bytegate.model;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {

    private final String method;
    private final URI uri;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private Map<String, String> pathParams;
    private final BodyPublisher bodyPublisher;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));
        this.bodyPublisher = builder.bodyPublisher;
        this.queryParams = Collections.unmodifiableMap(parseQueryParams(uri));
        this.pathParams = Collections.emptyMap();
    }

    private static Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> params = new LinkedHashMap<>();
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                String key = pair.substring(0, eq);
                String value = pair.substring(eq + 1);
                params.put(key, value);
            }
        }
        return params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return uri.getPath();
    }

    public URI getUri() {
        return uri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public String getPathParam(String name) {
        return pathParams.get(name);
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = Collections.unmodifiableMap(pathParams);
    }

    public Optional<BodyPublisher> bodyPublisher() {
        return Optional.ofNullable(bodyPublisher)
                .filter(bp -> bp.contentLength() > 0);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String method = "GET";
        private URI uri;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private BodyPublisher bodyPublisher;

        public Builder method(String method, BodyPublisher bodyPublisher) {
            this.method = method;
            this.bodyPublisher = bodyPublisher;
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    public interface BodyPublisher {
        byte[] getBytes();

        long contentLength();
    }

    public static class BodyPublishers {

        public static BodyPublisher noBody() {
            return new BodyPublisher() {
                @Override
                public byte[] getBytes() {
                    return new byte[0];
                }

                @Override
                public long contentLength() {
                    return 0;
                }

                @Override
                public String toString() {
                    return "";
                }
            };
        }

        public static BodyPublisher ofString(String body) {
            byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            return new BodyPublisher() {
                @Override
                public byte[] getBytes() {
                    return bytes;
                }

                @Override
                public long contentLength() {
                    return bytes.length;
                }

                @Override
                public String toString() {
                    return body;
                }
            };
        }
    }
}
