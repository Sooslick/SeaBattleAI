package ru.sooslick.seabattle;

public class EventResult {
    private Boolean success;
    private String token;
    private Integer session;
    private String info;

    public EventResult(Boolean success) {
        this.success = success;
    }

    public EventResult token(String token) {
        this.token = token;
        return this;
    }

    public EventResult session(Integer session) {
        this.session = session;
        return this;
    }

    public EventResult info(String info) {
        this.info = info;
        return this;
    }
}
