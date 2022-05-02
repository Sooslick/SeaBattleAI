package ru.sooslick.seabattle.result;

import ru.sooslick.seabattle.entity.SeaBattleSession;

import java.util.LinkedList;
import java.util.List;

public class EventResult {
    private Boolean success;
    private String token;
    private List<SessionInfo> sessionInfos;
    private String info;
    private GameResult gameResult;

    // Default constructor for AI JSON parser
    @SuppressWarnings("unused")
    public EventResult() {}

    public EventResult(Boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }

    public EventResult token(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public EventResult emptySession() {
        this.sessionInfos = new LinkedList<>();
        return this;
    }

    public EventResult session(SeaBattleSession session) {
        if (this.sessionInfos == null)
            this.sessionInfos = new LinkedList<>();
        this.sessionInfos.add(new SessionInfo(session));
        return this;
    }

    public List<SessionInfo> getSessionInfos() {
        return sessionInfos;
    }

    public EventResult info(String info) {
        this.info = info;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public EventResult gameResult(GameResult gameResult) {
        this.gameResult = gameResult;
        return this;
    }

    public GameResult getGameResult() {
        return gameResult;
    }

    public static class SessionInfo {
        private Integer sessionId;
        private boolean passworded;
        private boolean lookup;

        public SessionInfo() {}

        public SessionInfo(SeaBattleSession session) {
            this.sessionId = session.getId();
            this.passworded = !session.testPw(null);
            this.lookup = session.getPhase() == SeaBattleSession.SessionPhase.LOOKUP;
        }

        public Integer getSessionId() {
            return sessionId;
        }

        public boolean isPassworded() {
            return passworded;
        }

        public boolean isLookup() {
            return lookup;
        }
    }
}
