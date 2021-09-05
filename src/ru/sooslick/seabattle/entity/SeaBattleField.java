package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.result.FieldResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SeaBattleField {
    private final List<Integer> availableShips = Arrays.asList(4, 3, 3, 2, 2, 2, 1, 1, 1, 1);
    private final SeaBattleCell[][] cells = new SeaBattleCell[10][10];

    public SeaBattleField() {
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                cells[i][j] = new SeaBattleCell();
    }

    public FieldResult getResult(boolean myField) {
        List<FieldResult.Row> rows = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            List<Integer> cols = new LinkedList<>();
            for (int j = 0; j < 10; j++)
                cols.add(cells[i][j].getResult(myField));
            rows.add(new FieldResult.Row(cols));
        }
        return new FieldResult(rows);
    }

    public List<Integer> getShips() {
        return new LinkedList<>(availableShips);
    }
}
