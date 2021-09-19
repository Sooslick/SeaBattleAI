package ru.sooslick.seabattle.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(200, 16);
        httpExchange.getResponseBody().write("Work in progress".getBytes());
    }
}
