package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.SeaBattlePosition;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.result.EventResult;
import ru.sooslick.seabattle.result.FieldResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    public EventResult placeShip(String position, int size, boolean vertical) {
        // validate size
        if (!hasShip(size))
            return new EventResult(false).info("Failed placeShip: no such ship that size");
        // get ship cells
        SeaBattlePosition convertedPos = SeaBattlePosition.convertPosition(position);
        List<SeaBattlePosition> toCheck = new LinkedList<>();
        toCheck.add(convertedPos);
        for (int i = 1; i < size; i++)
            toCheck.add(vertical ? convertedPos.getRelative(i, 0) : convertedPos.getRelative(0, i));
        // validate cells
        if (toCheck.stream().anyMatch(pos -> getCell(pos.getRow(), pos.getCol()) == null))
            return new EventResult(false).info("Failed placeShip: out of bounds");
        // check cells
        if (toCheck.stream().anyMatch(pos -> !isCellPlaceable(pos.getRow(), pos.getCol())))
            return new EventResult(false).info("Failed placeShip: collision");
        // place ship
        availableShips.remove(size);
        // todo supress?
        toCheck.forEach(pos -> getCell(pos.getRow(), pos.getCol()).placeShip());
        return new EventResult(true);
    }

    private boolean isCellPlaceable(int row, int col) {
        List<SeaBattleCell> toCheck = new LinkedList<>();
        toCheck.add(getCell(row, col));
        toCheck.add(getCell(row - 1, col));
        toCheck.add(getCell(row + 1, col));
        toCheck.add(getCell(row, col - 1));
        toCheck.add(getCell(row, col + 1));
        if (!SeaBattleProperties.GAME_CORNER_COLLISION_ENABLE) {
            toCheck.add(getCell(row - 1, col - 1));
            toCheck.add(getCell(row - 1, col + 1));
            toCheck.add(getCell(row + 1, col - 1));
            toCheck.add(getCell(row + 1, col + 1));
        }
        return toCheck.stream()
                .filter(Objects::nonNull)
                .noneMatch(SeaBattleCell::hasShip);
    }

    private SeaBattleCell getCell(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10)
            return null;
        return cells[row][col];
    }

    private boolean hasShip(int size) {
        return availableShips.stream().anyMatch(i -> i == size);
    }
}
