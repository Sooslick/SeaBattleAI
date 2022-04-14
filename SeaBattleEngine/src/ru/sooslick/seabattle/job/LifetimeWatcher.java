package ru.sooslick.seabattle.job;

import ru.sooslick.seabattle.Logger;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;

import java.util.List;
import java.util.stream.Collectors;

public class LifetimeWatcher extends Thread {
    private static final long INTERVAL = 66666;  //ms

    boolean alive = true;

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

            if (inactiveSessions.size() > 0)
                Logger.info("LifetimeWatcher report: " + inactiveSessions.size() + " sessions expired");
            if (inactivePlayers.size() > 0)
                Logger.info("LifetimeWatcher report: " + inactivePlayers.size() + " players expired");

            try {
                //noinspection BusyWait
                sleep(INTERVAL);
            } catch (InterruptedException e) {
                alive = false;
                Logger.info("LifetimeWatcher interrupted");
            }
        }
    }

    public void kill() {
        alive = false;
    }
}
