package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.EventListener;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.result.EventResult;

import java.util.Arrays;
import java.util.List;

public class EventsTest {
    private final String token = EventListener.getToken().getToken();

    @Test
    public void tokenTest() {
        EventResult er = EventListener.getToken();
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        Assert.assertNotNull("Result does not contain token", er.getToken());
        Assert.assertNotNull("Token does not match any player", SeaBattleMain.getPlayer(er.getToken()));
        Assert.assertNull("Unexpected value in response", er.getInfo());
        Assert.assertNull("Unexpected value in response", er.getSession());
        Assert.assertNull("Unexpected value in response", er.getGameResult());
    }

    @Test
    public void registerSessionTest() {
        EventResult er = EventListener.registerSession("qwe", null);
        verifyEventFail(er, "Failed registerSession: unknown or expired token");

        SeaBattlePlayer player = new SeaBattlePlayer();
        new SeaBattleSession(player, null);
        er = EventListener.registerSession(player.getToken(), null);
        verifyEventFail(er, "Failed registerSession: token have linked to session already");

        er = EventListener.registerSession(token, "qwe");
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        Assert.assertNull("Unexpected value in response", er.getToken());
        Assert.assertNull("Unexpected value in response", er.getInfo());
        Assert.assertNotNull("Session tag not found", er.getSession());
        Assert.assertNull("Unexpected value in response", er.getGameResult());
        List<Integer> sessionIds = er.getSession();
        Assert.assertEquals("unexpected sessions count", 1, sessionIds.size());
        Assert.assertEquals("Token does not linked to created session",
                SeaBattleMain.getPlayer(token).getSession(), SeaBattleMain.getSession(sessionIds.get(0)));
    }

    @Test
    public void joinSessionTest() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattlePlayer p2 = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(p1, "qwe");

        EventResult er = EventListener.joinSession(null, null, null);
        verifyEventFail(er, "Failed joinSession: wrong sessionId format");
        er = EventListener.joinSession(null, "NaN", null);
        verifyEventFail(er, "Failed joinSession: wrong sessionId format");
        er = EventListener.joinSession(null, "1", null);
        verifyEventFail(er, "Failed joinSession: unknown or expired token");
        er = EventListener.joinSession(p1.getToken(), "1", null);
        verifyEventFail(er, "Failed joinSession: token have linked to session already");
        er = EventListener.joinSession(token, "1", null);
        verifyEventFail(er, "Failed joinSession: session with provided id is not exist");
        er = EventListener.joinSession(token, Integer.toString(session.getId()), "NotQwe");
        verifyEventFail(er, "Failed joinSession: session is private");
        session.joinPlayer(p2);
        er = EventListener.joinSession(token, Integer.toString(session.getId()), null);
        verifyEventFail(er, "Failed joinSession: game have started already");

        session = new SeaBattleSession(p1, "qwa");
        er = EventListener.joinSession(token, Integer.toString(session.getId()), "qwa");
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        Assert.assertNull("Unexpected value in response", er.getToken());
        Assert.assertNull("Unexpected value in response", er.getInfo());
        Assert.assertNull("Unexpected value in response", er.getSession());
        Assert.assertNull("Unexpected value in response", er.getGameResult());
        Assert.assertEquals("Token does not linked to created session",
                SeaBattleMain.getPlayer(token).getSession(), session);
    }

    @Test
    public void getSessionsTest() {
        // precondition cleanup
        SeaBattleMain.purgeSessions(SeaBattleMain.getActiveSessions());

        EventResult er = EventListener.getSessions("");
        verifyEventFail(er, "Failed getSessions: unknown or expired token");

        er = EventListener.getSessions(token);
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        Assert.assertNull("Unexpected value in response", er.getToken());
        Assert.assertNull("Unexpected value in response", er.getInfo());
        Assert.assertNotNull("Session tag not found", er.getSession());
        Assert.assertNull("Unexpected value in response", er.getGameResult());
        List<Integer> sessionIds = er.getSession();
        Assert.assertEquals("unexpected sessions count", 0, sessionIds.size());

        SeaBattlePlayer p1 = new SeaBattlePlayer();
        List<Integer> roomIds = Arrays.asList(
                new SeaBattleSession(p1, null).getId(),
                new SeaBattleSession(p1, "").getId(),
                new SeaBattleSession(p1, "q").getId()
        );
        er = EventListener.getSessions(token);
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        sessionIds = er.getSession();
        Assert.assertEquals("unexpected sessions count", 3, sessionIds.size());
        Assert.assertTrue("session list does not match existing sessions", sessionIds.containsAll(roomIds));
    }

    @Test
    public void getStatusTest() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(p1, "\n");

        EventResult er = EventListener.getSessionStatus("", null);
        verifyEventFail(er, "Failed getSessionStatus: unknown or expired token");
        er = EventListener.getSessionStatus(token, null);
        verifyEventFail(er, "Failed getSessionStatus: not joined to any session");
        er = EventListener.getSessionStatus(token, "\n1");
        verifyEventFail(er, "Failed getSessionStatus: wrong sessionId format");
        er = EventListener.getSessionStatus(token, "0");
        verifyEventFail(er, "Failed getSessionStatus: session with provided id is not exist");

        er = EventListener.getSessionStatus(token, Integer.toString(session.getId()));
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        Assert.assertNull("Unexpected value in response", er.getToken());
        Assert.assertNull("Unexpected value in response", er.getSession());
        Assert.assertNull("Unexpected value in response", er.getInfo());
        Assert.assertNotNull("GameResult not presented", er.getGameResult());

        er = EventListener.getSessionStatus(p1.getToken(), null);
        Assert.assertTrue("Unexpected operation result", er.getSuccess());
        Assert.assertNull("Unexpected value in response", er.getToken());
        Assert.assertNull("Unexpected value in response", er.getSession());
        Assert.assertNotNull("Info not presented", er.getInfo());
        Assert.assertNotNull("GameResult not presented", er.getGameResult());
    }

    @Test
    public void placeTest() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        new SeaBattleSession(p1, "&#169;");

        EventResult er = EventListener.placeShip(" ", null, null, null);
        verifyEventFail(er, "Can't placeShip: unknown or expired token");
        er = EventListener.placeShip(token, null, null, null);
        verifyEventFail(er, "Can't placeShip: not joined to any session");
        er = EventListener.placeShip(p1.getToken(), null, null, null);
        verifyEventFail(er, "Can't placeShip: size not specified");
        er = EventListener.placeShip(p1.getToken(), null, "0", null);
        verifyEventFail(er, "Can't placeShip: position not specified");
        er = EventListener.placeShip(p1.getToken(), "oO", "0", null);
        verifyEventFail(er, "Can't placeShip: wrong position format");
        er = EventListener.placeShip(p1.getToken(), "a1", "0", null);
        verifyEventFail(er, "Can't placeShip: wrong game phase");
    }

    @Test
    public void shootTest() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        new SeaBattleSession(p1, "☼");

        EventResult er = EventListener.shoot("\"", null);
        verifyEventFail(er, "Can't shoot: unknown or expired token");
        er = EventListener.shoot(token, null);
        verifyEventFail(er, "Can't shoot: not joined to any session");
        er = EventListener.shoot(p1.getToken(), null);
        verifyEventFail(er, "Can't shoot: position not specified");
        er = EventListener.shoot(p1.getToken(), "1");
        verifyEventFail(er, "Can't shoot: wrong position format");
        er = EventListener.shoot(p1.getToken(), "h10");
        verifyEventFail(er, "Can't shoot: wrong game phase");
    }

    private void verifyEventFail(EventResult er, String info) {
        Assert.assertFalse("Unexpected event result", er.getSuccess());
        Assert.assertEquals("Unexpected event info", info, er.getInfo());
        Assert.assertNull("Unexpected value in response", er.getToken());
        Assert.assertNull("Unexpected value in response", er.getSession());
        Assert.assertNull("Unexpected value in response", er.getGameResult());
    }
}
