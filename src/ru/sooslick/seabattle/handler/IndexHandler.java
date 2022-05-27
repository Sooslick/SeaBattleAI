package ru.sooslick.seabattle.handler;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.sooslick.seabattle.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * HTTP handler for common web requests and UI
 */
public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String reqPath = httpExchange.getRequestURI().getPath();
        if ("/".equals(reqPath))
            reqPath = "/index.html";
        Log.info("Request web resource " + reqPath);
        InputStream is = getClass().getResourceAsStream(reqPath);
        if (is == null) {
            try {
                is = new FileInputStream(reqPath.substring(1));
            } catch (IOException e) {
                Log.warn("Can't load resource " + reqPath);
                httpExchange.sendResponseHeaders(404, 3);
                httpExchange.getResponseBody().write("404".getBytes());
                httpExchange.close();
                return;
            }
        }
        byte[] answer = ByteStreams.toByteArray(is);
        httpExchange.sendResponseHeaders(200, answer.length);
        httpExchange.getResponseBody().write(answer);
        httpExchange.close();
    }
}
