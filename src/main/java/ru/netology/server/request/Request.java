package ru.netology.server.request;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, List<String>> queryParams;
    private final InputStream body;

    private Request(String method,
                    String path,
                    Map<String, List<String>> queryParams,
                    Map<String, String> headers,
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

    public List<String> getQueryParam(String name) {
        return queryParams.get(name);
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

        final Map<String, List<String>> paramsMap = new HashMap<>();
        if (path.contains("?")) {
            final String params = path.substring(path.indexOf("?") + 1);
            URLEncodedUtils.parse(params, StandardCharsets.UTF_8)
                    .forEach(param -> paramsMap.computeIfAbsent(param.getName(),
                            anything -> new ArrayList<>()).add(param.getValue()));
        }

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            // Accept: application/json
            int i = line.indexOf(":");
            String name = line.substring(0, i);
            String value = line.substring(i + 2);
            headers.put(name, value);
        }

        return new Request(method, path, paramsMap, headers, inputStream);
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
