package ru.sooslick.seabattle.handler;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.sooslick.seabattle.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String uri = httpExchange.getRequestURI().getPath();
        uri = uri.substring(1);
        if ("".equals(uri))
            uri = "index.html";
        if (! new File(uri).exists())
            uri = "index.html";
        Logger.info("Request web resource " + uri + " <- " + httpExchange.getRequestURI().getPath());
        byte[] answer = ByteStreams.toByteArray(new FileInputStream(uri));
        httpExchange.sendResponseHeaders(200, answer.length);
        httpExchange.getResponseBody().write(answer);
        httpExchange.close();
    }
}
