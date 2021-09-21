package ru.sooslick.seabattle;

import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.result.EventResult;

public class EventListener {
    public static EventResult getToken() {
        SeaBattlePlayer player = new SeaBattlePlayer();
        return new EventResult(true).token(player.getToken());
    }

    public static EventResult registerSession(String token, String pw) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed registerSession: unknown or expired token");
        if (player.getSession() != null)
            return new EventResult(false).info("Failed registerSession: token have linked to session already");
        SeaBattleSession session = new SeaBattleSession(player, pw);
        return new EventResult(true).session(session.getId());
    }

    public static EventResult joinSession(String token, String sessionId, String pw) {
        Integer id = tryParse(sessionId);
        if (id == null)
            return new EventResult(false).info("Failed joinSession: wrong sessionId format");
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed joinSession: unknown or expired token");
        if (player.getSession() != null)
            return new EventResult(false).info("Failed joinSession: token have linked to session already");
        SeaBattleSession session = SeaBattleMain.getSession(id);
        if (session == null)
            return new EventResult(false).info("Failed joinSession: session with provided id is not exist");
        if (session.getPhase() != SeaBattleSession.SessionPhase.LOOKUP)
            return new EventResult(false).info("Failed joinSession: game have started already");
        if (!session.testPw(pw))
            return new EventResult(false).info("Failed joinSession: session is private");
        session.joinPlayer(player);
        return new EventResult(true);
    }

    public static EventResult getSessions(String token) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed getSessions: unknown or expired token");
        EventResult result = new EventResult(true);
        SeaBattleMain.getActiveSessions().forEach(session -> result.session(session.getId()));
        return result;
    }

    public static EventResult getSessionStatus(String token) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed getSessionStatus: unknown or expired token");
        if (player.getSession() == null)
            return new EventResult(false).info("Failed getSessionStatus: not joined to any session");
        return new EventResult(true).gameResult(player.getSession().getResult(player));                 //todo player.getSession.getStatus(player)
    }

    public static EventResult placeShip(String token, String position, String sizeRaw, String verticalRaw) {
        Integer size = tryParse(sizeRaw);
        if (size == null)
            return new EventResult(false).info("Can't placeShip: size not specified");
        if (position == null)
            return new EventResult(false).info("Can't placeShip: position not specified");
        if (!SeaBattlePosition.isValid(position))
            return new EventResult(false).info("Can't placeShip: wrong position format");
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Can't placeShip: unknown or expired token");
        if (player.getSession() == null)
            return new EventResult(false).info("Can't placeShip: not joined to any session");
        boolean b = Boolean.parseBoolean(verticalRaw);
        return player.getSession().placeShip(player, position, size, b);
    }

    public static EventResult shoot(String token, String position) {
        if (position == null)
            return new EventResult(false).info("Can't shoot: position not specified");
        if (!SeaBattlePosition.isValid(position))
            return new EventResult(false).info("Can't shoot: wrong position format");
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Can't placeShip: unknown or expired token");
        if (player.getSession() == null)
            return new EventResult(false).info("Can't placeShip: not joined to any session");
        return player.getSession().shoot(player, position);
    }

    private EventListener() {}

    private static Integer tryParse(String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
