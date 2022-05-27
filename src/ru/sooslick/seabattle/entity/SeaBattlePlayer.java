package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;

import java.util.UUID;

/**
 * Player entity
 */
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

    /**
     * @return true if token does not expire
     */
    public boolean isAlive() {
        return getExpiration() > 0;
    }

    /**
     * @return milliseconds before token expiration
     */
    public long getExpiration() {
        return SeaBattleProperties.TOKEN_LIFETIME_TOTAL * 1000L - (System.currentTimeMillis() - lastActionTime);
    }

    /**
     * Resets token expiration
     */
    public void updateLastAction() {
        lastActionTime = System.currentTimeMillis();
    }

    protected void joinSession(SeaBattleSession session) {
        this.session = session;
        updateLastAction();
    }
}
