package ru.sooslick.seabattle;

import java.util.Arrays;

public enum ApiMethod {
    GET_TOKEN("/api/getToken"),
    REGISTER_SESSION("/api/registerSession"),
    JOIN_SESSION("/api/joinSession"),
    GET_SESSIONS("/api/getSessions"),
    GET_SESSION_STATUS("/api/getSessionStatus"),
    PLACE_SHIP("/api/placeShip"),
    SHOOT("/api/shoot");

    private final String path;

    public static ApiMethod find(String path) {
        return Arrays.stream(values()).filter(m -> m.path.equals(path)).findAny().orElse(null);
    }

    ApiMethod(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
