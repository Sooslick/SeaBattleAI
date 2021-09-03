package ru.sooslick.seabattle;

import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.job.LifetimeWatcher;

import java.util.LinkedList;
import java.util.List;

public class SeaBattleMain {
    private static final List<SeaBattleSession> ACTIVE_SESSIONS = new LinkedList<>();
    private static final List<SeaBattlePlayer> ACTIVE_PLAYERS = new LinkedList<>();

    private static LifetimeWatcher lifetimeWatcher;

    public static void addActiveSession(SeaBattleSession session) {
        ACTIVE_SESSIONS.add(session);
    }

    public static SeaBattleSession getSession(int id) {
        return ACTIVE_SESSIONS.stream()
                .filter(ses -> ses.getId() == id)
                .findAny()
                .orElse(null);
    }

    public static List<SeaBattleSession> getActiveSessions() {
        return ACTIVE_SESSIONS;
    }

    public static void purgeSessions(List<SeaBattleSession> inactiveSessions) {
        ACTIVE_SESSIONS.removeAll(inactiveSessions);
    }

    public static void registerPlayer(SeaBattlePlayer player) {
        ACTIVE_PLAYERS.add(player);
    }

    public static SeaBattlePlayer getPlayer(String token) {
        return ACTIVE_PLAYERS.stream()
                .filter(pl -> pl.getToken().equals(token))
                .findAny()
                .orElse(null);
    }

    public static List<SeaBattlePlayer> getActivePlayers() {
        return ACTIVE_PLAYERS;
    }

    public static void purgePlayers(List<SeaBattlePlayer> inactivePlayers) {
        ACTIVE_PLAYERS.removeAll(inactivePlayers);
    }

    public static void main(String[] args) {
        lifetimeWatcher = new LifetimeWatcher();
        lifetimeWatcher.start();
        //todo start server
    }
}
