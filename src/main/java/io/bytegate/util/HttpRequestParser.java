package io.bytegate.util;

import static java.net.URI.create;

import io.bytegate.model.HttpRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpRequestParser {

    public static HttpRequest parse(InputStream input) throws java.io.IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new RuntimeException("Empty request");
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            throw new RuntimeException("Invalid request line: " + requestLine);
        }

        String method = parts[0];
        String path = parts[1];

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(create(path));

        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(":");
            if (colonIndex > 0) {
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                builder.header(headerName, headerValue);
                if (headerName.equalsIgnoreCase("Content-Length")) {
                    contentLength = Integer.parseInt(headerValue);
                }
            }
        }

        HttpRequest.BodyPublisher bodyPublisher;
        if (contentLength > 0) {
            char[] body = new char[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int read = reader.read(body, totalRead, contentLength - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            bodyPublisher = HttpRequest.BodyPublishers.ofString(new String(body, 0, totalRead));
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        }

        builder.method(method, bodyPublisher);
        return builder.build();
    }
}
