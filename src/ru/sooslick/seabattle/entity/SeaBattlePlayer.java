package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;

import java.util.UUID;

public class SeaBattlePlayer {
    private final String token;

    private SeaBattleSession session;
    private long lastActionTime;

    public SeaBattlePlayer() {
        token = UUID.randomUUID().toString().substring(8);
        updateLastAction();

        SeaBattleMain.registerPlayer(this);
    }

    public String getToken() {
        return token;
    }

    public SeaBattleSession getSession() {
        return session;
    }

    public void joinSession(SeaBattleSession session) {
        this.session = session;
        updateLastAction();
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - lastActionTime < SeaBattleProperties.TOKEN_LIFETIME_TOTAL * 1000L;
    }

    private void updateLastAction() {
        lastActionTime = System.currentTimeMillis();
    }
}
