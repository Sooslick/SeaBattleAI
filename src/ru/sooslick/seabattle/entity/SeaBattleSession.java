package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;

public class SeaBattleSession {
    private static int nextId = 0;

    private final int id;
    private final long startTime;

    private SeaBattlePlayer p1;
    private SeaBattlePlayer p2;
    private String pw;
    private SessionPhase phase;
    private SeaBattleField p1Field;
    private SeaBattleField p2Field;
    private long lastActionTime;

    private static int getNextId() {
        return nextId++;
    }

    public SeaBattleSession(SeaBattlePlayer initiator, String k) {
        p1 = initiator;
        pw = k;
        id = getNextId();
        startTime = System.currentTimeMillis();
        updateLastAction();
        phase = SessionPhase.LOOKUP;

        p1.joinSession(this);
        SeaBattleMain.addActiveSession(this);
    }

    public int getId() {
        return id;
    }

    public SessionPhase getPhase() {
        return phase;
    }

    public void joinPlayer(SeaBattlePlayer player) {
        p2 = player;
        updateLastAction();
        phase = SessionPhase.PREPARE;

        p2.joinSession(this);
    }

    public boolean isAlive() {
        long currentTime = System.currentTimeMillis();
        long msAlive = currentTime - startTime;
        long msAction = currentTime - lastActionTime;
        if (msAlive / 1000 > SeaBattleProperties.SESSION_LIFETIME_TOTAL)
            return false;
        switch (phase) {
            case LOOKUP:
                if (msAction / 1000 > SeaBattleProperties.SESSION_LIFETIME_LOOKUP)
                    return false;
                break;
            case PREPARE:
                if (msAction / 1000 > SeaBattleProperties.SESSION_LIFETIME_PREPARE)
                    return false;
                break;
            case TURN_P1:
            case TURN_P2:
                if (msAction / 1000 > SeaBattleProperties.SESSION_LIFETIME_PLAYER)
                    return false;
        }
        return true;
    }

    public boolean testPw(String k) {
        return pw.isEmpty() || pw.equals(k);
    }

    private void updateLastAction() {
        lastActionTime = System.currentTimeMillis();
    }

    public enum SessionPhase {
        LOOKUP,
        PREPARE,
        TURN_P1,
        TURN_P2,
        ENDGAME;
    }
}
