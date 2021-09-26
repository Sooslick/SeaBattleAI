package ru.sooslick.seabattle.ai;

import ru.sooslick.seabattle.SeaBattlePosition;
import ru.sooslick.seabattle.entity.SeaBattleCell;

import java.util.*;
import java.util.function.Supplier;

public class AiField {
    private static final Random RANDOM = new Random();

    private final Supplier<PlacePosition> placeMethod;
    private final Supplier<String> shootMethod;

    private final SeaBattleCell[][] field;
    private final List<Integer> ships;

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
        if (ships.isEmpty())
            return null;
        return placeMethod.get();
    }

    public void confirmPlace(PlacePosition placePosition) {
        SeaBattlePosition sbPos = SeaBattlePosition.convertPosition(placePosition.getPosition());
        int imod = placePosition.isVert() ? 1 : 0;
        int jmod = placePosition.isVert() ? 0 : 1;
        List<SeaBattleCell> mark = new LinkedList<>();
        for (int z = 0; z < placePosition.getSize(); z++) {
            SeaBattlePosition basePos = sbPos.getRelative(z * imod, z * jmod);
            mark.add(getCell(basePos));
            mark.add(getCell(basePos.getRelative(-1, 0)));
            mark.add(getCell(basePos.getRelative(-1, 1)));
            mark.add(getCell(basePos.getRelative(0, 1)));
            mark.add(getCell(basePos.getRelative(1, 1)));
            mark.add(getCell(basePos.getRelative(1, 0)));
            mark.add(getCell(basePos.getRelative(1, -1)));
            mark.add(getCell(basePos.getRelative(0, -1)));
            mark.add(getCell(basePos.getRelative(-1, -1)));
        }
        mark.stream().filter(Objects::nonNull).forEach(SeaBattleCell::placeShip);
        int rmIndex = -1;
        for (int i = 0; i < ships.size(); i++)
            if (ships.get(i).equals(placePosition.getSize())) {
                rmIndex = i;
                break;
            }
        ships.remove(rmIndex);
    }

    public String getShootPosition() {
        return shootMethod.get();
    }

    public void confirmShoot(String shootPosition) {
        return;
    }

    private PlacePosition getRandomPlacePosition() {
        // filter cells
        List<PlacePosition> positions = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (!field[i][j].hasShip())
                    positions.add(new PlacePosition(new SeaBattlePosition(i, j)));
        Collections.shuffle(positions);
        for (PlacePosition pp : positions) {
            pp.size = getMaxShip();
            if (pp.size == 0)
                return null;
            pp.vert = RANDOM.nextBoolean();
            if (canPlace(pp))
                return pp;
            pp.vert = !pp.vert;
            if (canPlace(pp))
                return pp;
        }
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

    private int getMaxShip() {
        return ships.stream().mapToInt(i -> i).max().orElse(0);
    }

    private boolean canPlace(PlacePosition pp) {
        int imod = pp.vert ? 1 : 0;
        int jmod = pp.vert ? 0 : 1;
        for (int z = 1; z < pp.size; z++) {
            SeaBattlePosition sbPos = pp.positionRaw.getRelative(z * imod, z * jmod);
            if (sbPos.getRow() >= 10 || sbPos.getCol() >= 10)
                return false;
            if (field[sbPos.getRow()][sbPos.getCol()].hasShip())
                return false;
        }
        return true;
    }

    private SeaBattleCell getCell(SeaBattlePosition sbPos) {
        int i = sbPos.getRow();
        int j = sbPos.getCol();
        if (i < 0 || i >= 10 || j < 0 || j >= 10)
            return null;
        else
            return field[sbPos.getRow()][sbPos.getCol()];
    }

    public static class PlacePosition {
        private SeaBattlePosition positionRaw;
        private String position;
        private int size;
        private boolean vert;

        public PlacePosition(String position, int size, boolean vert) {
            this.position = position;
            this.size = size;
            this.vert = vert;
        }

        private PlacePosition(SeaBattlePosition rawpos) {
            positionRaw = rawpos;
            position = rawpos.toString();
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
