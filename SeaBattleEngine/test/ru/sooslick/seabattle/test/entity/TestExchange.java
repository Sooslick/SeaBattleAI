package ru.sooslick.seabattle.test.entity;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class TestExchange extends HttpExchange {
    private final String path;
    private final String query;
    private final OutputStream eventOut = new ByteArrayOutputStream();
    private boolean closed = false;
    private int code = -1;
    private long length = -1;

    public TestExchange(String path, String... queryParams) {
        this.path = path;
        this.query = "http://localhost" + path + (queryParams.length > 0 ?
                "?" + String.join("&", queryParams) : "");
    }

    public void verifyResult(int code, String... bodyParts) {
        Assert.assertTrue("Exchange is not closed", closed);
        Assert.assertEquals("Unexpected HTTP code", code, this.code);
        String responseBody = eventOut.toString();
        Assert.assertEquals("Content length not equals", responseBody.length(), length);
        Arrays.stream(bodyParts).forEach(str ->
                Assert.assertTrue("Response body does not contain " + str, responseBody.contains(str)));
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public URI getRequestURI() {
        try {
            return new URI(query);
        } catch (URISyntaxException e) {
            Assert.fail(e.getMessage());
            return null;
        }
    }

    @Override
    public HttpContext getHttpContext() {
        return new TestHttpContext(path);
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public OutputStream getResponseBody() {
        return eventOut;
    }

    @Override
    public void sendResponseHeaders(int i, long l) throws IOException {
        this.code = i;
        this.length = l;
    }

    // UNUSED

    @Override
    public Headers getRequestHeaders() {
        return null;
    }

    @Override
    public Headers getResponseHeaders() {
        return null;
    }

    @Override
    public String getRequestMethod() {
        return null;
    }

    @Override
    public InputStream getRequestBody() {
        return null;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public int getResponseCode() {
        return 0;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return null;
    }

    @Override
    public void setAttribute(String s, Object o) {
    }

    @Override
    public void setStreams(InputStream inputStream, OutputStream outputStream) {
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return null;
    }
}
