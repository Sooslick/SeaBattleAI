package ru.sooslick.seabattle.result;

import java.util.LinkedList;
import java.util.List;

public class EventResult {
    private Boolean success;
    private String token;
    private List<Integer> session;
    private String info;
    private GameResult gameResult;

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

    public EventResult session(Integer session) {
        if (this.session == null)
            this.session = new LinkedList<>();
        this.session.add(session);
        return this;
    }

    public List<Integer> getSession() {
        return session;
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
}