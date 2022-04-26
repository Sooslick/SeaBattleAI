package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.ApiMethod;
import ru.sooslick.seabattle.handler.ApiHandler;
import ru.sooslick.seabattle.handler.IndexHandler;
import ru.sooslick.seabattle.test.entity.TestExchange;

import java.io.IOException;
import java.util.Arrays;

public class HandlerTest {
    @Test
    public void testMethodFinder() {
        Arrays.stream(ApiMethod.values()).forEach(method ->
                Assert.assertEquals("Wrong method found", method, ApiMethod.find(method.getPath())));
    }

    @Test
    public void testApi() throws IOException {
        ApiHandler handler = new ApiHandler();

        // non-existing url
        TestExchange exchange = new TestExchange("/api/unknown");
        handler.handle(exchange);
        exchange.verifyResult(200, "\"success\" : false", "\"info\" : \"Not supported\"");

        // existing URL
        exchange = new TestExchange(ApiMethod.GET_TOKEN.getPath());
        handler.handle(exchange);
        exchange.verifyResult(200, "\"success\" : true", "\"token\" : \"");
    }

    @Test
    public void testMain() throws IOException {
        IndexHandler handler = new IndexHandler();

        // index.html
        TestExchange exchange = new TestExchange("/");
        handler.handle(exchange);
        exchange.verifyResult(200, "<title>Battleships</title>");

        // non index file
        exchange = new TestExchange("/style.css");
        handler.handle(exchange);
        exchange.verifyResult(200, "body {");

        // not existing resource
        exchange = new TestExchange("/unknown");
        handler.handle(exchange);
        exchange.verifyResult(404, "404");
    }
}
