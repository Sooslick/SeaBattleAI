package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.result.GameResult;

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
        p1Field = new SeaBattleField();
        p2Field = new SeaBattleField();

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

    public GameResult getResult(SeaBattlePlayer requester) {
        // requester is spectator
        if (p1 != requester && p2 != requester)
            return new GameResult(phase.toString(), null, p1Field.getResult(false), p2Field.getResult(false));

        Boolean turn = null;
        switch (phase) {
            case PREPARE:
                turn = Boolean.TRUE;
                break;
            case TURN_P1:
                turn = requester == p1;
                break;
            case TURN_P2:
                turn = requester == p2;
                break;
        }

        SeaBattleField myField;
        SeaBattleField enemyField;
        if (requester == p1) {
            myField = p1Field;
            enemyField = p2Field;
        } else {
            myField = p2Field;
            enemyField = p1Field;
        }
        GameResult result = new GameResult(phase.toString(), turn, myField.getResult(true), enemyField.getResult(false));
        return phase == SessionPhase.PREPARE ? result.ships(myField.getShips()) : result;
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
