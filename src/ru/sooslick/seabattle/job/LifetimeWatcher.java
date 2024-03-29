package ru.sooslick.seabattle.job;

import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Players and sessions keeper
 */
public class LifetimeWatcher extends Thread {
    private static final long INTERVAL = SeaBattleProperties.APP_CLEANUP_INTERVAL;
    private static final long IDLE_TIMEOUT = SeaBattleProperties.APP_INACTIVITY_SHUTDOWN_TIMEOUT;
    private static final boolean IDLE_ENABLED = SeaBattleProperties.APP_INACTIVITY_SHUTDOWN_ENABLE;

    boolean alive = true;
    long lastSessionTs = System.currentTimeMillis();

    @Override
    public void run() {
        while (alive) {
            List<SeaBattleSession> inactiveSessions = SeaBattleMain.getActiveSessions().stream()
                    .filter(session -> !session.isAlive())
                    .collect(Collectors.toList());
            SeaBattleMain.purgeSessions(inactiveSessions);

            List<SeaBattlePlayer> inactivePlayers = SeaBattleMain.getActivePlayers().stream()
                    .filter(player -> !player.isAlive())
                    .collect(Collectors.toList());
            SeaBattleMain.purgePlayers(inactivePlayers);

            AiKeeper.cleanup();
            AiKeeper.analyze();

            if (inactiveSessions.size() > 0)
                Log.info("LifetimeWatcher report: " + inactiveSessions.size() + " sessions expired");
            if (inactivePlayers.size() > 0)
                Log.info("LifetimeWatcher report: " + inactivePlayers.size() + " players expired");

            if (alive) {
                try {
                    TimeUnit.SECONDS.sleep(INTERVAL);
                } catch (InterruptedException e) {
                    alive = false;
                }
            }

            if (IDLE_ENABLED) {
                long currentTs = System.currentTimeMillis();
                if (SeaBattleMain.getActiveSessions().size() > 0 || SeaBattleMain.getActivePlayers().size() > 0)
                    lastSessionTs = currentTs;
                else {
                    long idleSeconds = (currentTs - lastSessionTs) / 1000;
                    Log.info(idleSeconds + " seconds idle");
                    if (idleSeconds > IDLE_TIMEOUT)
                        UserPromptListener.forceQuit();
                }
            }
        }
        Log.info("LifetimeWatcher task finished");
    }

    public void kill() {
        alive = false;
        this.interrupt();
        AiKeeper.kill();
    }
}
