package io.bytegate.util;

import io.bytegate.model.HttpResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseWriter {

    public static void write(OutputStream output, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("HTTP/1.1 ")
                .append(response.getStatusCode())
                .append(" ")
                .append(response.getReasonPhrase())
                .append("\r\n");

        byte[] bodyBytes = response.getBody().getBytes(StandardCharsets.UTF_8);

        for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
            sb.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }

        sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        sb.append("\r\n");

        output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        output.write(bodyBytes);
        output.flush();
    }
}
