package ru.sooslick.seabattle.entity;

import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.result.EventResult;
import ru.sooslick.seabattle.result.FieldResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class SeaBattleField {
    private final List<Integer> availableShips = new LinkedList<>(Arrays.asList(4, 3, 3, 2, 2, 2, 1, 1, 1, 1));
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

    // todo String validPosition -> SeaBattelPosition
    public EventResult placeShip(String validPosition, int size, boolean vertical) {
        // validate size
        if (!hasShip(size))
            return new EventResult(false).info("Failed placeShip: no such ship that size");
        // get ship cells
        SeaBattlePosition convertedPos = SeaBattlePosition.convertPosition(validPosition);
        List<SeaBattlePosition> toCheck = new LinkedList<>();
        toCheck.add(convertedPos);
        for (int i = 1; i < size; i++)
            toCheck.add(vertical ? convertedPos.getRelative(i, 0) : convertedPos.getRelative(0, i));
        // validate cells
        if (toCheck.stream().anyMatch(pos -> getCell(pos.getRow(), pos.getCol()) == null))
            return new EventResult(false).info("Failed placeShip: out of bounds");
        // check cells
        if (toCheck.stream().anyMatch(pos -> !isCellPlaceable(pos)))
            return new EventResult(false).info("Failed placeShip: collision");
        // place ship
        removeShip(size);
        toCheck.forEach(pos -> getCell(pos).placeShip());
        return new EventResult(true);
    }

    // todo convert
    public EventResult shoot(String position) {
        SeaBattlePosition convertedPos = SeaBattlePosition.convertPosition(position);
        SeaBattleCell cell = getCell(convertedPos);
        if (SeaBattleProperties.GAME_STRIKED_CHECK_ENABLE && cell.isStriked())
            return new EventResult(false).info("Failed shoot: this cell is striked");
        cell.strike();
        return new EventResult(true).info(getShootInfo(convertedPos));
    }

    private boolean isCellPlaceable(SeaBattlePosition pos) {
        int row = pos.getRow();
        int col = pos.getCol();
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

    private SeaBattleCell getCell(SeaBattlePosition pos) {
        return getCell(pos.getRow(), pos.getCol());
    }

    private SeaBattleCell getCell(int row, int col) {
        if (row < 0 || row >= 10 || col < 0 || col >= 10)
            return null;
        return cells[row][col];
    }

    private String getShootInfo(SeaBattlePosition pos) {
        SeaBattleCell baseCell = getCell(pos);
        if (!baseCell.hasShip())
            return "miss";
        if (!hasAliveShips())
            return "win";

        List<SeaBattlePosition> shipPoses = new LinkedList<>();
        shipPoses.add(pos);
        SeaBattlePosition checkPos = pos.getRelative(-1, 0);
        SeaBattleCell checkCell = getCell(checkPos);
        while (checkCell != null && checkCell.hasShip()) {
            shipPoses.add(checkPos);
            checkPos = checkPos.getRelative(-1, 0);
            checkCell = getCell(checkPos);
        }
        checkPos = pos.getRelative(1, 0);
        checkCell = getCell(checkPos);
        while (checkCell != null && checkCell.hasShip()) {
            shipPoses.add(checkPos);
            checkPos = checkPos.getRelative(1, 0);
            checkCell = getCell(checkPos);
        }
        checkPos = pos.getRelative(0, -1);
        checkCell = getCell(checkPos);
        while (checkCell != null && checkCell.hasShip()) {
            shipPoses.add(checkPos);
            checkPos = checkPos.getRelative(0, -1);
            checkCell = getCell(checkPos);
        }
        checkPos = pos.getRelative(0, 1);
        checkCell = getCell(checkPos);
        while (checkCell != null && checkCell.hasShip()) {
            shipPoses.add(checkPos);
            checkPos = checkPos.getRelative(0, 1);
            checkCell = getCell(checkPos);
        }
        if (shipPoses.stream().allMatch(currPos -> getCell(currPos).isStriked())) {
            strikeNearby(shipPoses);
            return "kill";
        } else
            return "hit";
    }

    private void strikeNearby(List<SeaBattlePosition> shipPoses) {
        for (SeaBattlePosition pos : shipPoses) {
            strikeCell(pos.getRelative(-1, 0));
            strikeCell(pos.getRelative(1, 0));
            strikeCell(pos.getRelative(0, -1));
            strikeCell(pos.getRelative(0, 1));
            if (!SeaBattleProperties.GAME_CORNER_COLLISION_ENABLE) {
                strikeCell(pos.getRelative(-1, -1));
                strikeCell(pos.getRelative(1, -1));
                strikeCell(pos.getRelative(1, 1));
                strikeCell(pos.getRelative(-1, 1));
            }
        }
    }

    private void strikeCell(SeaBattlePosition pos) {
        SeaBattleCell cell = getCell(pos);
        if (cell != null)
            cell.strike();
    }

    private boolean hasAliveShips() {
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (cells[i][j].hasShip() && !cells[i][j].isStriked())
                    return true;
        return false;
    }

    private boolean hasShip(int size) {
        return availableShips.stream().anyMatch(i -> i == size);
    }

    private void removeShip(int size) {
        for (int i = 0; i < availableShips.size(); i++)
            if (availableShips.get(i).equals(size)) {
                availableShips.remove(i);
                return;
            }
    }
}
