package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;

import java.util.concurrent.TimeUnit;

public class PlayerTest {
    @Test
    public void test() {
        SeaBattlePlayer player = new SeaBattlePlayer();

        // first join
        SeaBattleSession session = new SeaBattleSession(player, null);
        Assert.assertEquals("sessions not equals", session, player.getSession());

        // second join
        SeaBattlePlayer player2 = new SeaBattlePlayer();
        session = new SeaBattleSession(player2, null);
        session.joinPlayer(player);
        Assert.assertEquals("sessions not equals", session, player.getSession());
    }

    @Test
    public void testAlive() throws InterruptedException {
        SeaBattleProperties.TOKEN_LIFETIME_TOTAL = 1;
        SeaBattlePlayer player = new SeaBattlePlayer();
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse("Player still alive", player.isAlive());

        player.updateLastAction();
        Assert.assertTrue("Player is not alive", player.isAlive());
    }
}
