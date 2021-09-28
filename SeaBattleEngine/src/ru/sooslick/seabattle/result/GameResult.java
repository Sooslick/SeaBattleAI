package ru.sooslick.seabattle.result;

import java.util.List;

public class GameResult {
    private String phase;
    private Boolean myTurn;
    private List<Integer> ships;
    private FieldResult myField;
    private FieldResult enemyField;
    private String matchLog;

    public GameResult() {}

    public GameResult(String phase, Boolean myTurn, FieldResult myField, FieldResult enemyField) {
        this.phase = phase;
        this.myTurn = myTurn;
        this.myField = myField;
        this.enemyField = enemyField;
    }

    public String getPhase() {
        return phase;
    }

    public Boolean isMyTurn() {
        return myTurn;
    }

    public GameResult ships(List<Integer> ships) {
        this.ships = ships;
        return this;
    }

    public List<Integer> getShips() {
        return ships;
    }

    public FieldResult getMyField() {
        return myField;
    }

    public FieldResult getEnemyField() {
        return enemyField;
    }

    public GameResult matchLog(String ml) {
        matchLog = ml;
        return this;
    }

    public String getMatchLog() {
        return matchLog;
    }
}
