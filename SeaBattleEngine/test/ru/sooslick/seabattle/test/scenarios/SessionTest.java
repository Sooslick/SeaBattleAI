package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattlePosition;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.result.EventResult;

import java.util.concurrent.TimeUnit;

public class SessionTest {
    @Test
    public void testCreate() {
        SeaBattlePlayer creator = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(creator, null);
        Assert.assertEquals("Player not linked to the session", session, creator.getSession());

        SeaBattlePlayer joiner = new SeaBattlePlayer();
        session.joinPlayer(joiner);
        Assert.assertEquals("Player not linked to the session", session, joiner.getSession());

        SeaBattlePlayer spectator = new SeaBattlePlayer();
        session.joinPlayer(spectator);
        Assert.assertNotEquals("Player linked to the session", session, spectator.getSession());

        Assert.assertTrue("Failed testPw", session.testPw(null));
        Assert.assertFalse("Failed testPw", session.testPw(""));
        Assert.assertFalse("Failed testPw", session.testPw("Qwerty"));
        Assert.assertFalse("Failed testPw", session.testPw(" "));
        Assert.assertFalse("Failed testPw", session.testPw("\n"));
        Assert.assertFalse("Failed testPw", session.testPw("0"));

        session = new SeaBattleSession(creator, "");
        Assert.assertTrue("Failed testPw", session.testPw(""));
        Assert.assertFalse("Failed testPw", session.testPw(null));
        Assert.assertFalse("Failed testPw", session.testPw("Qwerty"));
        Assert.assertFalse("Failed testPw", session.testPw("0"));
        Assert.assertFalse("Failed testPw", session.testPw(" "));
        Assert.assertFalse("Failed testPw", session.testPw("\n"));

        session = new SeaBattleSession(creator, "aA");
        Assert.assertTrue("Failed testPw", session.testPw("aA"));
        Assert.assertFalse("Failed testPw", session.testPw("AA"));
        Assert.assertFalse("Failed testPw", session.testPw("aa"));
        Assert.assertFalse("Failed testPw", session.testPw(" aA"));
        Assert.assertFalse("Failed testPw", session.testPw("aA "));
        Assert.assertFalse("Failed testPw", session.testPw("aA\n"));
    }

    @Test
    public void testSelfJoin() {
        SeaBattlePlayer creator = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(creator, null);
        Assert.assertEquals("Player not linked to the session", session, creator.getSession());

        session.joinPlayer(creator);
        Assert.assertEquals("Player not linked to the session", session, creator.getSession());
        Assert.assertEquals("Phase changed", SeaBattleSession.SessionPhase.LOOKUP, session.getPhase());
    }

    @Test
    public void testPhase() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(p1, "\n");
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.LOOKUP, session.getPhase());

        SeaBattlePlayer p2 = new SeaBattlePlayer();
        session.joinPlayer(p2);
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.PREPARE, session.getPhase());

        session.placeShip(p1, cp("a1"), 1, true);
        session.placeShip(p1, cp("c1"), 1, true);
        session.placeShip(p1, cp("e1"), 1, true);
        session.placeShip(p1, cp("g1"), 1, true);
        session.placeShip(p1, cp("i1"), 2, true);
        session.placeShip(p1, cp("i4"), 2, true);
        session.placeShip(p1, cp("g4"), 2, true);
        session.placeShip(p1, cp("e4"), 3, true);
        session.placeShip(p1, cp("c4"), 3, true);
        session.placeShip(p1, cp("a4"), 4, true);
        session.placeShip(p2, cp("a1"), 1, true);
        session.placeShip(p2, cp("c1"), 1, true);
        session.placeShip(p2, cp("e1"), 1, true);
        session.placeShip(p2, cp("g1"), 1, true);
        session.placeShip(p2, cp("i1"), 2, true);
        session.placeShip(p2, cp("i4"), 2, true);
        session.placeShip(p2, cp("g4"), 2, true);
        session.placeShip(p2, cp("e4"), 3, true);
        session.placeShip(p2, cp("c4"), 3, true);
        session.placeShip(p2, cp("a4"), 4, true);
        // first turn determined by p1's hash. Force TURN_P1
        if (p1.getToken().hashCode() % 2 != 0) {
            Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.TURN_P2, session.getPhase());
            session.shoot(p2, cp("j10"));
        }
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.TURN_P1, session.getPhase());

        session.shoot(p1, cp("a1"));
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.TURN_P1, session.getPhase());
        session.shoot(p1, cp("a4"));
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.TURN_P1, session.getPhase());
        session.shoot(p1, cp("b4"));
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.TURN_P2, session.getPhase());
        session.shoot(p2, cp("a1"));
        session.shoot(p2, cp("c1"));
        session.shoot(p2, cp("e1"));
        session.shoot(p2, cp("g1"));
        session.shoot(p2, cp("i1"));
        session.shoot(p2, cp("i2"));
        session.shoot(p2, cp("i4"));
        session.shoot(p2, cp("i5"));
        session.shoot(p2, cp("g4"));
        session.shoot(p2, cp("g5"));
        session.shoot(p2, cp("e4"));
        session.shoot(p2, cp("e5"));
        session.shoot(p2, cp("e6"));
        session.shoot(p2, cp("c4"));
        session.shoot(p2, cp("c5"));
        session.shoot(p2, cp("c6"));
        session.shoot(p2, cp("a4"));
        session.shoot(p2, cp("a5"));
        session.shoot(p2, cp("a6"));
        session.shoot(p2, cp("a7"));
        Assert.assertEquals("Wrong phase", SeaBattleSession.SessionPhase.ENDGAME, session.getPhase());
    }

    @Test
    public void testPlace() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattlePlayer p2 = new SeaBattlePlayer();
        SeaBattlePlayer p3 = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(p1, "");
        verifyEventResult(session.placeShip(p1, cp("a1"), 1, true), false, "Can't placeShip: wrong game phase");
        session.joinPlayer(p2);

        verifyEventResult(session.placeShip(p3, cp("a1"), 1, true), false, "Can't placeShip: not a player");
        verifyEventResult(session.placeShip(p1, cp("a1"), 1, true), true, null);
        verifyEventResult(session.placeShip(p2, cp("a1"), 1, true), true, null);
        session.placeShip(p1, cp("c1"), 1, true);
        session.placeShip(p1, cp("e1"), 1, true);
        session.placeShip(p1, cp("g1"), 1, true);
        session.placeShip(p1, cp("i1"), 2, true);
        session.placeShip(p1, cp("i4"), 2, true);
        session.placeShip(p1, cp("g4"), 2, true);
        session.placeShip(p1, cp("e4"), 3, true);
        session.placeShip(p1, cp("c4"), 3, true);
        session.placeShip(p1, cp("a4"), 4, true);
        verifyEventResult(session.placeShip(p1, cp("a1"), 1, true), false, "Failed placeShip: no such ship that size");
        session.placeShip(p2, cp("c1"), 1, true);
        session.placeShip(p2, cp("e1"), 1, true);
        session.placeShip(p2, cp("g1"), 1, true);
        session.placeShip(p2, cp("i1"), 2, true);
        session.placeShip(p2, cp("i4"), 2, true);
        session.placeShip(p2, cp("g4"), 2, true);
        session.placeShip(p2, cp("e4"), 3, true);
        session.placeShip(p2, cp("c4"), 3, true);
        session.placeShip(p2, cp("a4"), 4, true);
        verifyEventResult(session.placeShip(p2, cp("a1"), 1, true), false, "Can't placeShip: wrong game phase");
    }

    @Test
    public void testShoot() {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattlePlayer p2 = new SeaBattlePlayer();
        SeaBattlePlayer p3 = new SeaBattlePlayer();
        SeaBattleSession session = new SeaBattleSession(p1, "");
        session.joinPlayer(p2);

        session.placeShip(p1, cp("a1"), 1, true);
        session.placeShip(p1, cp("c1"), 1, true);
        session.placeShip(p1, cp("e1"), 1, true);
        session.placeShip(p1, cp("g1"), 1, true);
        session.placeShip(p1, cp("i1"), 2, true);
        session.placeShip(p1, cp("i4"), 2, true);
        session.placeShip(p1, cp("g4"), 2, true);
        session.placeShip(p1, cp("e4"), 3, true);
        session.placeShip(p1, cp("c4"), 3, true);
        session.placeShip(p1, cp("a4"), 4, true);
        verifyEventResult(session.shoot(p2, cp("a1")), false, "Can't shoot: wrong game phase");
        session.placeShip(p2, cp("a1"), 1, true);
        session.placeShip(p2, cp("c1"), 1, true);
        session.placeShip(p2, cp("e1"), 1, true);
        session.placeShip(p2, cp("g1"), 1, true);
        session.placeShip(p2, cp("i1"), 2, true);
        session.placeShip(p2, cp("i4"), 2, true);
        session.placeShip(p2, cp("g4"), 2, true);
        session.placeShip(p2, cp("e4"), 3, true);
        session.placeShip(p2, cp("c4"), 3, true);
        session.placeShip(p2, cp("a4"), 4, true);
        if (p1.getToken().hashCode() % 2 != 0)
            session.shoot(p2, cp("j10"));
        verifyEventResult(session.shoot(p2, cp("a1")), false, "Can't shoot: opponent turn");
        verifyEventResult(session.shoot(p3, cp("a1")), false, "Can't shoot: not a player");
        verifyEventResult(session.shoot(p1, cp("a1")), true, "kill");
        verifyEventResult(session.shoot(p1, cp("a4")), true, "hit");
        verifyEventResult(session.shoot(p1, cp("b4")), true, "miss");
        session.shoot(p2, cp("a1"));
        session.shoot(p2, cp("c1"));
        session.shoot(p2, cp("e1"));
        session.shoot(p2, cp("g1"));
        session.shoot(p2, cp("i1"));
        session.shoot(p2, cp("i2"));
        session.shoot(p2, cp("i4"));
        session.shoot(p2, cp("i5"));
        session.shoot(p2, cp("g4"));
        session.shoot(p2, cp("g5"));
        session.shoot(p2, cp("e4"));
        session.shoot(p2, cp("e5"));
        session.shoot(p2, cp("e6"));
        session.shoot(p2, cp("c4"));
        session.shoot(p2, cp("c5"));
        session.shoot(p2, cp("c6"));
        session.shoot(p2, cp("a4"));
        session.shoot(p2, cp("a5"));
        session.shoot(p2, cp("a6"));
        verifyEventResult(session.shoot(p2, cp("a7")), true, "win");
    }

    @Test
    public void testAlive() throws InterruptedException {
        SeaBattlePlayer p1 = new SeaBattlePlayer();
        SeaBattlePlayer p2 = new SeaBattlePlayer();

        SeaBattleProperties.SESSION_LIFETIME_LOOKUP = 1;
        SeaBattleSession session = new SeaBattleSession(p1, "");
        Assert.assertTrue("Room is not alive", session.isAlive());
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse("Room is alive", session.isAlive());

        SeaBattleProperties.SESSION_LIFETIME_PREPARE = 1;
        session.joinPlayer(p2);
        Assert.assertTrue("Room is not alive", session.isAlive());
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse("Room is alive", session.isAlive());

        SeaBattleProperties.SESSION_LIFETIME_PLAYER = 1;
        session.placeShip(p1, cp("a1"), 1, true);
        session.placeShip(p1, cp("c1"), 1, true);
        session.placeShip(p1, cp("e1"), 1, true);
        session.placeShip(p1, cp("g1"), 1, true);
        session.placeShip(p1, cp("i1"), 2, true);
        session.placeShip(p1, cp("i4"), 2, true);
        session.placeShip(p1, cp("g4"), 2, true);
        session.placeShip(p1, cp("e4"), 3, true);
        session.placeShip(p1, cp("c4"), 3, true);
        session.placeShip(p1, cp("a4"), 4, true);
        session.placeShip(p2, cp("a1"), 1, true);
        session.placeShip(p2, cp("c1"), 1, true);
        session.placeShip(p2, cp("e1"), 1, true);
        session.placeShip(p2, cp("g1"), 1, true);
        session.placeShip(p2, cp("i1"), 2, true);
        session.placeShip(p2, cp("i4"), 2, true);
        session.placeShip(p2, cp("g4"), 2, true);
        session.placeShip(p2, cp("e4"), 3, true);
        session.placeShip(p2, cp("c4"), 3, true);
        session.placeShip(p2, cp("a4"), 4, true);
        Assert.assertTrue("Room is not alive", session.isAlive());
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse("Room is alive", session.isAlive());

        SeaBattleProperties.SESSION_LIFETIME_LOOKUP = 100;
        SeaBattleProperties.SESSION_LIFETIME_TOTAL = 1;
        session = new SeaBattleSession(p1, "");
        Assert.assertTrue("Room is not alive", session.isAlive());
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse("Room is alive", session.isAlive());
    }

    public void verifyEventResult(EventResult er, boolean success, String info) {
        Assert.assertEquals("Unexpected event result", success, er.getSuccess());
        Assert.assertEquals("Unexpected event info", info, er.getInfo());
    }

    private SeaBattlePosition cp(String convert) {
        return SeaBattlePosition.convertPosition(convert);
    }
}
