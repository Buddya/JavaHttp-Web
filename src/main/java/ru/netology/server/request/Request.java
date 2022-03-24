package ru.netology.server.request;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, List<String>> queryParams;
    private final InputStream body;

    private Request(String method,
                    String path,
                    Map<String, String> headers,
                    Map<String, List<String>> queryParams,
                    InputStream body) {

        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public InputStream getBody() {
        return body;
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException {
        final var in = new BufferedReader(new InputStreamReader(inputStream));

        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            throw new IOException("Invalid request");
        }

        String method = parts[0];
        String path = parts[1];
        String cleanPath = getPathWithoutQuery(path);

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            // Accept: application/json
            int i = line.indexOf(":");
            String name = line.substring(0, i);
            String value = line.substring(i + 2);
            headers.put(name, value);
        }

        return new Request(method, cleanPath, headers, getQueryParams(path), inputStream);
    }

    private static String getPathWithoutQuery(String path) {
        if (path.contains("?")) {
            return path.substring(0, path.indexOf("?"));
        }
        return path;
    }

    private static Map<String, List<String>> getQueryParams(String path) {
        if (!path.contains("?")) {
            return Collections.emptyMap();
        }

        final String queryParams = path.substring(path.indexOf("?") + 1);
        final HashMap<String, List<String>> queryParamsMap = new HashMap<>();
        URLEncodedUtils.parse(queryParams, StandardCharsets.UTF_8)
                .forEach(param -> queryParamsMap.computeIfAbsent(param.getName(),
                        qValue -> new ArrayList<>()).add(param.getValue()));
        return queryParamsMap;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                '}';
    }
}
