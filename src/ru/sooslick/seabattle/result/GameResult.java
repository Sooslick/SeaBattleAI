package ru.sooslick.seabattle.result;

import java.util.List;

public class GameResult {
    private String phase;
    private Boolean myTurn;
    private List<Integer> ships;
    private FieldResult myField;
    private FieldResult enemyField;

    public GameResult(String phase, Boolean myTurn, FieldResult myField, FieldResult enemyField) {
        this.phase = phase;
        this.myTurn = myTurn;
        this.myField = myField;
        this.enemyField = enemyField;
    }

    public GameResult ships(List<Integer> ships) {
        this.ships = ships;
        return this;
    }
}
