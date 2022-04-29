package ru.sooslick.seabattle.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.SeaBattleMain;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.result.EventResult;
import ru.sooslick.seabattle.result.FieldResult;
import ru.sooslick.seabattle.result.GameResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SeaBattleSession {
    private final List<Object> activeLocks = new LinkedList<>();
    private final int id = getNextId();
    private final long startTime = System.currentTimeMillis();
    private final SeaBattleField p1Field = new SeaBattleField();
    private final SeaBattleField p2Field = new SeaBattleField();
    private final StringBuilder matchLog = new StringBuilder();
    private final String pw;
    private final SeaBattlePlayer p1;
    private SeaBattlePlayer p2;
    private SessionPhase phase = SessionPhase.LOOKUP;
    private long lastActionTime;

    private static int getNextId() {
        return Math.abs(UUID.randomUUID().toString().substring(0, 8).hashCode());
    }

    public SeaBattleSession(@NotNull SeaBattlePlayer initiator, @Nullable String k) {
        p1 = initiator;
        pw = k;

        updateLastAction();
        p1.joinSession(this);
        SeaBattleMain.addActiveSession(this);
        Log.info("Registered new session " + id);
    }

    public int getId() {
        return id;
    }

    public SessionPhase getPhase() {
        return phase;
    }

    public void joinPlayer(@NotNull SeaBattlePlayer player) {
        if (p2 != null || player.equals(p1))
            return;

        p2 = player;
        updateLastAction();
        phase = SessionPhase.PREPARE;

        p2.joinSession(this);
        notifyAction();
    }

    public boolean isAlive() {
        long currentTime = System.currentTimeMillis();
        long msAlive = currentTime - startTime;
        long msAction = currentTime - lastActionTime;
        if (msAlive > SeaBattleProperties.SESSION_LIFETIME_TOTAL * 1000L)
            return false;
        switch (phase) {
            case LOOKUP:
                if (msAction > SeaBattleProperties.SESSION_LIFETIME_LOOKUP * 1000L)
                    return false;
                break;
            case PREPARE:
                if (msAction > SeaBattleProperties.SESSION_LIFETIME_PREPARE * 1000L)
                    return false;
                break;
            case TURN_P1:
            case TURN_P2:
            case ENDGAME:
                if (msAction > SeaBattleProperties.SESSION_LIFETIME_PLAYER * 1000L)
                    return false;
        }
        return true;
    }

    public String getLifetimeInfo() {
        long currentTime = System.currentTimeMillis();
        long msAlive = currentTime - startTime;
        long msAction = currentTime - lastActionTime;
        long total = SeaBattleProperties.SESSION_LIFETIME_TOTAL - msAlive / 1000;
        long stageDuration = phase == SessionPhase.LOOKUP ? SeaBattleProperties.SESSION_LIFETIME_LOOKUP :
                phase == SessionPhase.PREPARE ? SeaBattleProperties.SESSION_LIFETIME_PREPARE :
                        phase != SessionPhase.ENDGAME ? SeaBattleProperties.SESSION_LIFETIME_PLAYER : 0;
        long stage = Math.max(stageDuration - msAction / 1000, 0);
        return ("Room expires in " + total + "s. Current phase timeleft: " + stage + "s");
    }

    public boolean testPw(@Nullable String k) {
        return Objects.equals(pw, k);
    }

    public EventResult getStatus(SeaBattlePlayer requester) {
        requester.updateLastAction();
        // requester is spectator
        if (p1 != requester && p2 != requester) {
            if (phase == SessionPhase.LOOKUP || phase == SessionPhase.PREPARE)
                return new EventResult(true).gameResult(new GameResult(phase.toString()));
            boolean reveal = phase == SessionPhase.ENDGAME;
            return new EventResult(true).gameResult(new GameResult(phase.toString(), null, p1Field.getResult(reveal), p2Field.getResult(reveal))
                    .matchLog(formatLog(requester)));
        }

        updateLastAction();
        SeaBattleField myField;
        SeaBattleField enemyField;
        if (requester == p1) {
            myField = p1Field;
            enemyField = p2Field;
        } else {
            myField = p2Field;
            enemyField = p1Field;
        }
        Boolean turn = null;
        switch (phase) {
            case PREPARE:
                turn = myField.getShips().size() > 0;
                break;
            case TURN_P1:
                turn = requester == p1;
                break;
            case TURN_P2:
                turn = requester == p2;
                break;
        }

        FieldResult efr = phase == SessionPhase.PREPARE ? null : enemyField.getResult(phase == SessionPhase.ENDGAME);
        GameResult result = new GameResult(phase.toString(), turn, myField.getResult(true), efr)
                .matchLog(formatLog(requester));
        return new EventResult(true)
                .info(getLifetimeInfo())
                .gameResult(phase == SessionPhase.PREPARE ? result.ships(myField.getShips()) : result);
    }

    public EventResult placeShip(SeaBattlePlayer player, SeaBattlePosition position, int size, boolean vertical) {
        player.updateLastAction();
        // requester is spectator
        if (p1 != player && p2 != player)
            return new EventResult(false).info("Can't placeShip: not a player");
        if (phase != SessionPhase.PREPARE)
            return new EventResult(false).info("Can't placeShip: wrong game phase");

        updateLastAction();
        EventResult result;
        if (player == p1) {
            result = p1Field.placeShip(position, size, vertical);
            if (result.getSuccess())
                matchLog.append("p1: +").append(position).append(" x").append(size).append(vertical ? " |\n" : " -\n");
        } else {
            result = p2Field.placeShip(position, size, vertical);
            if (result.getSuccess())
                matchLog.append("p2: +").append(position).append(" x").append(size).append(vertical ? " |\n" : " -\n");
        }

        if (p1Field.getShips().isEmpty() && p2Field.getShips().isEmpty())
            phase = p1.getToken().hashCode() % 2 == 0 ? SessionPhase.TURN_P1 : SessionPhase.TURN_P2;
        notifyAction();
        return result;
    }

    public EventResult shoot(SeaBattlePlayer player, SeaBattlePosition position) {
        player.updateLastAction();
        // requester is spectator
        if (p1 != player && p2 != player)
            return new EventResult(false).info("Can't shoot: not a player");
        if (phase != SessionPhase.TURN_P1 && phase != SessionPhase.TURN_P2)
            return new EventResult(false).info("Can't shoot: wrong game phase");
        if (!allowShoot(player))
            return new EventResult(false).info("Can't shoot: opponent turn");

        updateLastAction();
        EventResult result;
        // shoot
        String p;
        if (player == p1) {
            p = "p1: ";
            result = p2Field.shoot(position);
        } else {
            p = "p2: ";
            result = p1Field.shoot(position);
        }
        // post-shoot action
        if (result.getSuccess())
            matchLog.append(p).append(position).append(" ").append(result.getInfo()).append("\n");
        if ("win".equals(result.getInfo()))
            phase = SessionPhase.ENDGAME;
        else if ("miss".equals(result.getInfo()))
            switchTurn();
        notifyAction();
        return result;
    }

    public void waitForStatus() {
        Object lock = new Object();
        if (phase == SessionPhase.ENDGAME)
            return;
        synchronized (lock) {
            activeLocks.add(lock);
            try {
                lock.wait();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void notifyAction() {
        activeLocks.forEach(lock -> {
            synchronized (lock) {
                lock.notify();
            }
        });
        activeLocks.clear();
    }

    private void updateLastAction() {
        lastActionTime = System.currentTimeMillis();
    }

    private boolean allowShoot(SeaBattlePlayer player) {
        return ((player == p1 && phase == SessionPhase.TURN_P1) || (player == p2 && phase == SessionPhase.TURN_P2));
    }

    private void switchTurn() {
        phase = phase == SessionPhase.TURN_P1 ? SessionPhase.TURN_P2 : SessionPhase.TURN_P1;
    }

    private String formatLog(@NotNull SeaBattlePlayer viewer) {
        if (phase == SessionPhase.ENDGAME)
            return matchLog.toString();
        String prefix = "p[1-2]";
        if (viewer.equals(p1))
            prefix = "p2";
        else if (viewer.equals(p2))
            prefix = "p1";
        return matchLog.toString().replaceAll(prefix + ": \\+.*?\\n", "");
    }

    public enum SessionPhase {
        LOOKUP,
        PREPARE,
        TURN_P1,
        TURN_P2,
        ENDGAME
    }
}
