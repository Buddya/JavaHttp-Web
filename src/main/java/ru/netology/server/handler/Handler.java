package ru.netology.server.handler;

import ru.netology.server.request.Request;
import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface Handler {
    void handle(Request request, BufferedOutputStream out) throws IOException;
}
