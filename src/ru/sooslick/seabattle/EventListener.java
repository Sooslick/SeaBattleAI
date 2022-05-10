package ru.sooslick.seabattle;

import org.jetbrains.annotations.Nullable;
import ru.sooslick.seabattle.entity.SeaBattlePlayer;
import ru.sooslick.seabattle.entity.SeaBattlePosition;
import ru.sooslick.seabattle.entity.SeaBattleSession;
import ru.sooslick.seabattle.job.AiKeeper;
import ru.sooslick.seabattle.result.EventResult;

import java.util.function.Supplier;

public class EventListener {
    public static EventResult getToken() {
        SeaBattlePlayer player = new SeaBattlePlayer();
        return new EventResult(true).token(player.getToken());
    }

    public static EventResult getRules(@Nullable String token) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed getRules: unknown or expired token");
        return new EventResult(true).info(SeaBattleProperties.getRules(player));
    }

    public static EventResult registerSession(@Nullable String token, @Nullable String pw) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed registerSession: unknown or expired token");
        if (player.getSession() != null)
            return new EventResult(false).info("Failed registerSession: token have linked to session already");
        SeaBattleSession session = new SeaBattleSession(player, pw);
        return new EventResult(true).session(session);
    }

    public static EventResult joinSession(@Nullable String token, @Nullable String sessionId, @Nullable String pw) {
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

    public static EventResult getSessions(@Nullable String token) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Failed getSessions: unknown or expired token");
        EventResult result = new EventResult(true).emptySession();
        SeaBattleMain.getActiveSessions().forEach(result::session);
        return result;
    }

    public static EventResult getSessionStatus(@Nullable String token, @Nullable String sessionId) {
        // return result immediately anyway
        return validateStatusRequest(token, sessionId).supplier.get();
    }

    public static EventResult getSessionStatusLongpoll(@Nullable String token, @Nullable String sessionId, @Nullable String timeoutRaw) {
        StatusSupplier result = validateStatusRequest(token, sessionId);
        if (result.session == null)
            // session is null only when EventResult is unsuccessful
            return result.supplier.get();
        Integer timeoutObj = tryParse(timeoutRaw);
        long timeout = timeoutObj == null || timeoutObj <= 0 || timeoutObj > 60 ? 60 : timeoutObj;
        result.session.waitForStatus(result.player, timeout);
        return result.supplier.get();
    }

    public static EventResult placeShip(@Nullable String token, @Nullable String position, @Nullable String sizeRaw, @Nullable String verticalRaw) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Can't placeShip: unknown or expired token");
        if (player.getSession() == null)
            return new EventResult(false).info("Can't placeShip: not joined to any session");
        Integer size = tryParse(sizeRaw);
        if (size == null)
            return new EventResult(false).info("Can't placeShip: size not specified");
        if (position == null)
            return new EventResult(false).info("Can't placeShip: position not specified");
        if (!SeaBattlePosition.isValid(position))
            return new EventResult(false).info("Can't placeShip: wrong position format");
        boolean b = Boolean.parseBoolean(verticalRaw);
        return player.getSession().placeShip(player, SeaBattlePosition.convertPosition(position), size, b);
    }

    public static EventResult shoot(@Nullable String token, @Nullable String position) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Can't shoot: unknown or expired token");
        if (player.getSession() == null)
            return new EventResult(false).info("Can't shoot: not joined to any session");
        if (position == null)
            return new EventResult(false).info("Can't shoot: position not specified");
        if (!SeaBattlePosition.isValid(position))
            return new EventResult(false).info("Can't shoot: wrong position format");
        return player.getSession().shoot(player, SeaBattlePosition.convertPosition(position));
    }

    public static EventResult initAi(@Nullable String token, @Nullable String sessionId, @Nullable String pw, @Nullable String skill) {
        Integer id = tryParse(sessionId);
        if (id == null)
            return new EventResult(false).info("Can't init AI: wrong sessionId format");
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new EventResult(false).info("Can't init AI: unknown or expired token");
        SeaBattleSession session = SeaBattleMain.getSession(id);
        if (session == null)
            return new EventResult(false).info("Can't init AI: session with provided id is not exist");
        if (session.getPhase() != SeaBattleSession.SessionPhase.LOOKUP)
            return new EventResult(false).info("Can't init AI: game have started already");
        if (!session.testPw(pw))
            return new EventResult(false).info("Can't init AI: wrong session password");
        AiKeeper.launchAi(id, pw, skill != null);
        return new EventResult(true);
    }

    private EventListener() {}

    private static Integer tryParse(@Nullable String intString) {
        if (intString == null)
            return null;
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static StatusSupplier validateStatusRequest(@Nullable String token, @Nullable String sessionId) {
        SeaBattlePlayer player = SeaBattleMain.getPlayer(token);
        if (player == null)
            return new StatusSupplier(() -> new EventResult(false).info("Failed getSessionStatus: unknown or expired token"));
        if (player.getSession() == null) {
            if (sessionId == null)
                return new StatusSupplier(() -> new EventResult(false).info("Failed getSessionStatus: not joined to any session"));
            Integer id = tryParse(sessionId);
            if (id == null)
                return new StatusSupplier(() -> new EventResult(false).info("Failed getSessionStatus: wrong sessionId format"));
            SeaBattleSession session = SeaBattleMain.getSession(id);
            if (session == null)
                return new StatusSupplier(() -> new EventResult(false).info("Failed getSessionStatus: session with provided id is not exist"));
            return new StatusSupplier(session, () -> session.getStatus(player));
        }
        return new StatusSupplier(player, () -> player.getSession().getStatus(player));
    }

    private static class StatusSupplier {
        private final SeaBattlePlayer player;
        private final SeaBattleSession session;
        private final Supplier<EventResult> supplier;

        private StatusSupplier(Supplier<EventResult> supplier) {
            this.player = null;
            this.session = null;
            this.supplier = supplier;
        }

        private StatusSupplier(SeaBattleSession session, Supplier<EventResult> supplier) {
            this.player = null;
            this.session = session;
            this.supplier = supplier;
        }

        private StatusSupplier(SeaBattlePlayer player, Supplier<EventResult> supplier) {
            this.player = player;
            this.session = player.getSession();
            this.supplier = supplier;
        }
    }
}
