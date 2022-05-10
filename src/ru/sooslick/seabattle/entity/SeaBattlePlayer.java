package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;

import java.util.UUID;

public class SeaBattlePlayer {
    private final String token;

    private SeaBattleSession session;
    private long lastActionTime;

    public SeaBattlePlayer() {
        token = UUID.randomUUID().toString().substring(0, 8);
        updateLastAction();

        SeaBattleMain.registerPlayer(this);
        Log.info("Registered new player " + token);
    }

    public String getToken() {
        return token;
    }

    public SeaBattleSession getSession() {
        return session;
    }

    public boolean isAlive() {
        return getExpiration() > 0;
    }

    public long getExpiration() {
        return SeaBattleProperties.TOKEN_LIFETIME_TOTAL * 1000L - (System.currentTimeMillis() - lastActionTime);
    }

    public void updateLastAction() {
        lastActionTime = System.currentTimeMillis();
    }

    protected void joinSession(SeaBattleSession session) {
        this.session = session;
        updateLastAction();
    }
}
