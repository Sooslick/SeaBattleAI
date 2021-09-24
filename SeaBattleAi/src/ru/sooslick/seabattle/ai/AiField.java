package ru.sooslick.seabattle.ai;

import ru.sooslick.seabattle.entity.SeaBattleCell;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class AiField {
    private final Supplier<PlacePosition> placeMethod;
    private final Supplier<String> shootMethod;

    private SeaBattleCell[][] field;
    private List<Integer> ships;

    public AiField(boolean useHeatMap, List<Integer> ships) {
        this.ships = new LinkedList<>(ships);
        if (useHeatMap) {
            placeMethod = this::getHeatPlacePosition;
            shootMethod = this::getHeatShootPosition;
        } else {
            placeMethod = this::getRandomPlacePosition;
            shootMethod = this::getRandomShootPosition;
        }
        field = new SeaBattleCell[10][10];
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                field[i][j] = new SeaBattleCell();
    }

    public PlacePosition getPlacePosition() {
        return placeMethod.get();
    }

    public void confirmPlace(PlacePosition placePosition) {
        return;
    }

    public String getShootPosition() {
        return shootMethod.get();
    }

    public void confirmShoot(String shootPosition) {
        return;
    }

    private PlacePosition getRandomPlacePosition() {
        return null;
    }

    private PlacePosition getHeatPlacePosition() {
        return null;
    }

    private String getRandomShootPosition() {
        return null;
    }

    private String getHeatShootPosition() {
        return null;
    }

    public static class PlacePosition {
        private final String position;
        private final int size;
        private final boolean vert;

        public PlacePosition(String position, int size, boolean vert) {
            this.position = position;
            this.size = size;
            this.vert = vert;
        }

        public String getPosition() {
            return position;
        }

        public int getSize() {
            return size;
        }

        public boolean isVert() {
            return vert;
        }

        @Override
        public String toString() {
            return position + ", " + size + ", " + (vert ? "vert" : "hor");
        }
    }
}
