package ru.sooslick.seabattle;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.Nullable;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.handler.ApiHandler;
import ru.sooslick.seabattle.handler.IndexHandler;
import ru.sooslick.seabattle.job.LifetimeWatcher;
import ru.sooslick.seabattle.job.UserPromptListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
        return new LinkedList<>(ACTIVE_SESSIONS);
    }

    public static void purgeSessions(List<SeaBattleSession> inactiveSessions) {
        ACTIVE_SESSIONS.removeAll(inactiveSessions);
    }

    public static void registerPlayer(SeaBattlePlayer player) {
        ACTIVE_PLAYERS.add(player);
    }

    public static SeaBattlePlayer getPlayer(@Nullable String token) {
        return ACTIVE_PLAYERS.stream()
                .filter(pl -> pl.getToken().equals(token))
                .findAny()
                .orElse(null);
    }

    public static List<SeaBattlePlayer> getActivePlayers() {
        return new LinkedList<>(ACTIVE_PLAYERS);
    }

    public static void purgePlayers(List<SeaBattlePlayer> inactivePlayers) {
        ACTIVE_PLAYERS.removeAll(inactivePlayers);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        parseArgs(args);
        Log.info("Starting app in " + System.getProperty("user.dir"));
        LifetimeWatcher lifetimeWatcher = new LifetimeWatcher();
        lifetimeWatcher.start();

        HttpHandler eventHandler = new ApiHandler(ApiHandler.DEFAULT_API);
        HttpServer server = HttpServer.create();
        Log.info("Starting server on port " + SeaBattleProperties.APP_SERVER_PORT);
        server.bind(new InetSocketAddress(SeaBattleProperties.APP_SERVER_PORT), SeaBattleProperties.APP_SERVER_CONNECTIONS);
        server.createContext("/", new IndexHandler());
        server.createContext("/api/longpoll/getSessionStatus", new ApiHandler(ApiHandler.LONG_POLL_STATUS));
        Arrays.stream(ApiMethod.values())
                .forEach(m -> server.createContext(m.getPath(), eventHandler));
        ExecutorService exec = Executors.newFixedThreadPool(SeaBattleProperties.APP_SERVER_CONNECTIONS);
        server.setExecutor(exec);
        server.start();

        UserPromptListener upl = new UserPromptListener();
        upl.start();
        upl.join();

        Log.info("Stopping server...");
        lifetimeWatcher.kill();
        server.stop(0);
        exec.shutdownNow();
    }

    private static void parseArgs(String[] args) {
        HashMap<String, String> kvs = new HashMap<>();
        Arrays.stream(args).forEach(a -> {
            String[] kv = a.split("=", 2);
            kvs.put(kv[0], kv.length > 1 ? kv[1] : null);
        });
        if (kvs.containsKey("-app.properties")) {
            System.setProperty("app.properties", kvs.get("-app.properties"));
        }
        if (kvs.containsKey("-use.defaults")) {
            System.setProperty("use.defaults", "true");
        }
    }
}
