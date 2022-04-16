package ru.sooslick.seabattle;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.handler.ApiHandler;
import ru.sooslick.seabattle.handler.IndexHandler;
import ru.sooslick.seabattle.job.LifetimeWatcher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

public class SeaBattleMain {
    private static final List<SeaBattleSession> ACTIVE_SESSIONS = new LinkedList<>();
    private static final List<SeaBattlePlayer> ACTIVE_PLAYERS = new LinkedList<>();

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

    public static void main(String[] args) throws IOException {
        Log.info("Starting app in " + System.getProperty("user.dir"));
        LifetimeWatcher lifetimeWatcher = new LifetimeWatcher();
        lifetimeWatcher.start();
        HttpHandler eventHandler = new ApiHandler();
        HttpServer server = HttpServer.create();
        Log.info("Starting server on port " + SeaBattleProperties.APP_SERVER_PORT);
        server.bind(new InetSocketAddress(SeaBattleProperties.APP_SERVER_PORT), SeaBattleProperties.APP_SERVER_CONNECTIONS);
        server.createContext("/", new IndexHandler());
        Arrays.stream(ApiMethod.values())
                .forEach(m -> server.createContext(m.getPath(), eventHandler));
        server.setExecutor(Executors.newFixedThreadPool(SeaBattleProperties.APP_SERVER_CONNECTIONS));
        server.start();
    }
}
