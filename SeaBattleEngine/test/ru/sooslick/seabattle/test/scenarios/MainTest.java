package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.job.LifetimeWatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainTest {
    @Test
    public void testSessions() {
        // cleanup state
        SeaBattleMain.purgeSessions(SeaBattleMain.getActiveSessions());
        Assert.assertTrue("Sessions count not equals", SeaBattleMain.getActiveSessions().isEmpty());
//        SeaBattleMain.purgePlayers(SeaBattleMain.getActivePlayers());
//        Assert.assertTrue("Players count not equals", SeaBattleMain.getActivePlayers().isEmpty());

        // add session
        SeaBattlePlayer player = new SeaBattlePlayer();
        SeaBattleSession session1 = new SeaBattleSession(player, "");
        Assert.assertEquals("getSession returns unexpected result", session1, SeaBattleMain.getSession(session1.getId()));
        List<SeaBattleSession> sessionList = SeaBattleMain.getActiveSessions();
        Assert.assertEquals("Sessions count not equals", 1, sessionList.size());
        Assert.assertTrue("Sessions does not contain expected element", sessionList.contains(session1));

        // add another session
        SeaBattleSession session2 = new SeaBattleSession(player, "");
        Assert.assertEquals("getSession returns unexpected result", session2, SeaBattleMain.getSession(session2.getId()));
        sessionList = SeaBattleMain.getActiveSessions();
        Assert.assertEquals("Sessions count not equals", 2, sessionList.size());
        Assert.assertTrue("Sessions does not contain expected elements", sessionList.containsAll(Arrays.asList(session1, session2)));

        // rm session
        SeaBattleMain.purgeSessions(Collections.singletonList(session1));
        Assert.assertNull("getSession returns unexpected result", SeaBattleMain.getSession(session1.getId()));
        sessionList = SeaBattleMain.getActiveSessions();
        Assert.assertEquals("Sessions count not equals", 1, sessionList.size());
        Assert.assertTrue("Sessions does not contain expected element", sessionList.contains(session2));

        // rm nothing
        SeaBattleMain.purgeSessions(Collections.emptyList());
        sessionList = SeaBattleMain.getActiveSessions();
        Assert.assertEquals("Sessions count not equals", 1, sessionList.size());
        Assert.assertTrue("Sessions does not contain expected element", sessionList.contains(session2));
    }

    @Test
    public void testPlayers() {
        // cleanup state
        SeaBattleMain.purgePlayers(SeaBattleMain.getActivePlayers());
        Assert.assertTrue("Players count not equals", SeaBattleMain.getActivePlayers().isEmpty());

        // add player
        SeaBattlePlayer player1 = new SeaBattlePlayer();
        Assert.assertEquals("getPlayer returns unexpected result", player1, SeaBattleMain.getPlayer(player1.getToken()));
        List<SeaBattlePlayer> playerList = SeaBattleMain.getActivePlayers();
        Assert.assertEquals("Players count not equals", 1, playerList.size());
        Assert.assertTrue("Players does not contain expected element", playerList.contains(player1));

        // add another player
        SeaBattlePlayer player2 = new SeaBattlePlayer();
        Assert.assertEquals("getPlayer returns unexpected result", player2, SeaBattleMain.getPlayer(player2.getToken()));
        playerList = SeaBattleMain.getActivePlayers();
        Assert.assertEquals("Players count not equals", 2, playerList.size());
        Assert.assertTrue("Players does not contain expected elements", playerList.containsAll(Arrays.asList(player1, player2)));

        // rm player
        SeaBattleMain.purgePlayers(Collections.singletonList(player1));
        Assert.assertNull("getPlayer returns unexpected result", SeaBattleMain.getPlayer(player1.getToken()));
        playerList = SeaBattleMain.getActivePlayers();
        Assert.assertEquals("Players count not equals", 1, playerList.size());
        Assert.assertTrue("Players does not contain expected element", playerList.contains(player2));

        // rm nothing
        SeaBattleMain.purgePlayers(Collections.emptyList());
        playerList = SeaBattleMain.getActivePlayers();
        Assert.assertEquals("Players count not equals", 1, playerList.size());
        Assert.assertTrue("Players does not contain expected element", playerList.contains(player2));
    }

    @Test
    public void testWatcher() throws InterruptedException {
        // cleanup state
        SeaBattleMain.purgeSessions(SeaBattleMain.getActiveSessions());
        Assert.assertTrue("Sessions count not equals", SeaBattleMain.getActiveSessions().isEmpty());
        SeaBattleMain.purgePlayers(SeaBattleMain.getActivePlayers());
        Assert.assertTrue("Players count not equals", SeaBattleMain.getActivePlayers().isEmpty());

        // props
        SeaBattleProperties.APP_CLEANUP_INTERVAL = 1;
        SeaBattleProperties.SESSION_LIFETIME_TOTAL = 1;
        SeaBattleProperties.TOKEN_LIFETIME_TOTAL = 1;

        // register player & session
        SeaBattlePlayer player1 = new SeaBattlePlayer();
        SeaBattleSession session1 = new SeaBattleSession(player1, "");
        TimeUnit.MILLISECONDS.sleep(500);

        // register watcher
        LifetimeWatcher lifetimeWatcher = new LifetimeWatcher();
        lifetimeWatcher.start();
        Assert.assertEquals("Sessions count not equals", 1, SeaBattleMain.getActiveSessions().size());
        Assert.assertEquals("Players count not equals", 1, SeaBattleMain.getActivePlayers().size());
        TimeUnit.MILLISECONDS.sleep(500);

        // register another pl/ses
        SeaBattlePlayer player2 = new SeaBattlePlayer();
        SeaBattleSession session2 = new SeaBattleSession(player2, "");

        // first player & session stale
        TimeUnit.SECONDS.sleep(1);
        Assert.assertEquals("Sessions count not equals", 1, SeaBattleMain.getActiveSessions().size());
        Assert.assertEquals("Players count not equals", 1, SeaBattleMain.getActivePlayers().size());
        Assert.assertNull("Player1 found.", SeaBattleMain.getPlayer(player1.getToken()));
        Assert.assertEquals("Player2 not found.", player2, SeaBattleMain.getPlayer(player2.getToken()));
        Assert.assertNull("Session1 found.", SeaBattleMain.getSession(session1.getId()));
        Assert.assertEquals("Session2 not found.", session2, SeaBattleMain.getSession(session2.getId()));

        // kill
        lifetimeWatcher.kill();
        lifetimeWatcher.join(100);
        Assert.assertFalse(lifetimeWatcher.isAlive());
    }
}
