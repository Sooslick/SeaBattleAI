package ru.sooslick.seabattle.handler;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.sooslick.seabattle.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String reqPath = httpExchange.getRequestURI().getPath();
        String uri = reqPath.substring(1);
        if ("".equals(uri))
            uri = "index.html";
        if (! new File(uri).exists())
            uri = "index.html";
        Log.info("Request web resource " + uri + " <- " + reqPath);
        byte[] answer = ByteStreams.toByteArray(new FileInputStream(uri));
        httpExchange.sendResponseHeaders(200, answer.length);
        httpExchange.getResponseBody().write(answer);
        httpExchange.close();
    }
}
