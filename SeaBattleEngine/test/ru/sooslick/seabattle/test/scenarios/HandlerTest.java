package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.ApiMethod;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattlePosition;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.handler.ApiHandler;
import ru.sooslick.seabattle.handler.IndexHandler;
import ru.sooslick.seabattle.test.entity.TestExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class HandlerTest {
    @Test
    public void testMethodFinder() {
        Arrays.stream(ApiMethod.values()).forEach(method ->
                Assert.assertEquals("Wrong method found", method, ApiMethod.find(method.getPath())));
    }

    @Test
    public void testApi() throws IOException {
        ApiHandler handler = new ApiHandler(ApiHandler.DEFAULT_API);

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
    public void testLongPoll() throws IOException, InterruptedException {
        ApiHandler handler = new ApiHandler(ApiHandler.LONG_POLL_STATUS);

        TestExchange exchange = new TestExchange("/api/longpoll/getSessionStatus");
        handler.handle(exchange);
        exchange.verifyResult(200, "\"success\" : false", "\"info\" : \"");

        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattlePlayer p2 = new SeaBattlePlayer();
        SeaBattlePlayer p3 = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(p1, "");
        session.joinPlayer(p2);

        TestExchange lpExchange = new TestExchange("/api/longpoll/getSessionStatus",
                "token=" + p3.getToken(), "sessionId=" + session.getId());
        Thread thread = new Thread(() -> {
            try {
                handler.handle(lpExchange);
            } catch (IOException ignored) {
            }
        });
        thread.start();
        TimeUnit.MILLISECONDS.sleep(100);
        Assert.assertTrue("Longpoll responded immediately", thread.isAlive());
        Assert.assertFalse("Exchange closed", lpExchange.isClosed());

        session.placeShip(p1, SeaBattlePosition.convertPosition("a1"), 1, true);
        thread.join(100);
        Assert.assertFalse("Longpoll did not respond after event", thread.isAlive());
        lpExchange.verifyResult(200, "\"success\" : true");
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
