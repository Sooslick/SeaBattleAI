package ru.sooslick.seabattle.test.entity;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.util.List;
import java.util.Map;

public class TestHttpContext extends HttpContext {
    private final String path;

    public TestHttpContext(String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    // Unused

    @Override
    public HttpHandler getHandler() {
        return null;
    }

    @Override
    public void setHandler(HttpHandler httpHandler) {
    }

    @Override
    public HttpServer getServer() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public List<Filter> getFilters() {
        return null;
    }

    @Override
    public Authenticator setAuthenticator(Authenticator authenticator) {
        return null;
    }

    @Override
    public Authenticator getAuthenticator() {
        return null;
    }
}
