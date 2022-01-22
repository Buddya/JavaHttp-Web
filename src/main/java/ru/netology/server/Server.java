package ru.netology.server;

import ru.netology.server.handler.Handler;
import ru.netology.server.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;

    // {method -> {path -> handler, path2 -> handler2}}
    private final Map<String, Map<String, Handler>> handlers;
    private final Handler notFoundHandler = ((request, out) -> {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    });

    public Server(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        if (this.handlers.get(method) == null) {
            this.handlers.put(method, new ConcurrentHashMap<>());
        }
        this.handlers.get(method).put(path, handler);
    }

    public void listen(int port) {

        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> handlerConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handlerConnection(Socket socket) {
        try (
                socket;
                final var in = socket.getInputStream();
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            Request request = Request.fromInputStream(in);

            Map<String, Handler> handlerMap = handlers.get(request.getMethod());
            if (handlerMap == null) {
                notFoundHandler.handle(request, out);
                return;
            }

            Handler handler = handlerMap.get(request.getPath());
            if (handler == null) {
                notFoundHandler.handle(request, out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
