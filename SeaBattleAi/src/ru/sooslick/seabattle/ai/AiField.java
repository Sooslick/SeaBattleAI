package ru.sooslick.seabattle.ai;

import ru.sooslick.seabattle.SeaBattlePosition;
import ru.sooslick.seabattle.entity.SeaBattleCell;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AiField {
    private static final Random RANDOM = new Random();

    private final Supplier<PlacePosition> placeMethod;
    private final Supplier<String> shootMethod;

    private final SeaBattleCell[][] field;
    private final List<Integer> ships;
    private final int shipsTotal;

    private SeaBattlePosition detectedShip;
    private Boolean detectedVert;

    public AiField(boolean useHeatMap, List<Integer> ships) {
        this.ships = new LinkedList<>(ships);
        shipsTotal = ships.size();
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

    public void confirmShoot(String shootPosition, String shootResult) {
        // mark
        SeaBattlePosition sbPos = SeaBattlePosition.convertPosition(shootPosition);
        getCell(sbPos).strike();
        // detect ship orientation
        if (!shootResult.equals("miss")) {
            getCell(sbPos).placeShip();
            if (detectedShip == null) {
                detectedShip = sbPos;
            } else if (detectedVert == null) {
                detectedVert = detectedShip.getRow() != sbPos.getRow();
            }
        }
        // mark ship if killed
        if (shootResult.equals("win") || shootResult.equals("kill")) {
            int marked = 0;
            if (detectedShip != null) {
                markKill(detectedShip);
                marked++;
                if (detectedVert != null) {
                    int imod = detectedVert ? 1 : 0;
                    int jmod = detectedVert ? 0 : 1;
                    int z = 0;
                    while (true) {
                        z++;
                        SeaBattlePosition checkPos = detectedShip.getRelative(z * imod, z * jmod);
                        SeaBattleCell check = getCell(checkPos);
                        if (check == null)
                            break;
                        if (!check.hasShip())
                            break;
                        markKill(checkPos);
                        marked++;
                    }
                    z = 0;
                    while (true) {
                        z--;
                        SeaBattlePosition checkPos = detectedShip.getRelative(z * imod, z * jmod);
                        SeaBattleCell check = getCell(checkPos);
                        if (check == null)
                            break;
                        if (!check.hasShip())
                            break;
                        markKill(checkPos);
                        marked++;
                    }
                }
            }
            detectedVert = null;
            detectedShip = null;
            int rmIndex = -1;
            for (int i = 0; i < ships.size(); i++)
                if (ships.get(i).equals(marked)) {
                    rmIndex = i;
                    break;
                }
            ships.remove(rmIndex);
        }
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
        // guessing detected ship
        if (detectedShip != null) {
            List<SeaBattlePosition> guessNext = new LinkedList<>();
            if (detectedVert == null || detectedVert) {
                // check up / down
                for (int i = -1; i <= 1; i+= 2) {
                    int mod = 0;
                    while (true) {
                        mod++;
                        SeaBattlePosition cPos = detectedShip.getRelative(i * mod, 0);
                        SeaBattleCell cCell = getCell(cPos);
                        if (cCell == null)
                            break;
                        if (cCell.hasShip())
                            continue;
                        if (cCell.isStriked())
                            break;
                        guessNext.add(cPos);
                        break;
                    }
                }
            }
            if (detectedVert == null || !detectedVert) {
                // check left / right
                for (int i = -1; i <= 1; i+= 2) {
                    int mod = 0;
                    while (true) {
                        mod++;
                        SeaBattlePosition cPos = detectedShip.getRelative(0, i * mod);
                        SeaBattleCell cCell = getCell(cPos);
                        if (cCell == null)
                            break;
                        if (cCell.hasShip())
                            continue;
                        if (cCell.isStriked())
                            break;
                        guessNext.add(cPos);
                        break;
                    }
                }
            }
            Collections.shuffle(guessNext);
            return guessNext.get(0).toString();
        }
        // get available cells
        List<SeaBattlePosition> positions = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (!field[i][j].isStriked())
                    positions.add(new SeaBattlePosition(i, j));
        // guess cell by random
        int maxShip = getMaxShip();
        if (ships.size() >= shipsTotal || maxShip <= 1) {
            Collections.shuffle(positions);
            return positions.get(0).toString();
        }
        // try to analyze board
        else {
            Map<SeaBattlePosition, Integer> scoreMap = new HashMap<>();
            int maxScore = 0;
            for (SeaBattlePosition sbPos : positions) {
                int cScore = 0;
                for (int i = 1; i < maxShip; i++) {
                    SeaBattleCell check = getCell(sbPos.getRelative(-i, 0));
                    if (check != null && !check.isStriked())
                        cScore++;
                    check = getCell(sbPos.getRelative(i, 0));
                    if (check != null && !check.isStriked())
                        cScore++;
                    check = getCell(sbPos.getRelative(0, -i));
                    if (check != null && !check.isStriked())
                        cScore++;
                    check = getCell(sbPos.getRelative(0, i));
                    if (check != null && !check.isStriked())
                        cScore++;
                }
                scoreMap.put(sbPos, cScore);
                if (cScore > maxScore)
                    maxScore = cScore;
            }
            final int max = maxScore;
            List<SeaBattlePosition> selectedPos = scoreMap.entrySet().stream().filter(e -> e.getValue() >= max).map(Map.Entry::getKey).collect(Collectors.toList());
            Collections.shuffle(selectedPos);
            return selectedPos.get(0).toString();
        }
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

    private void markKill(SeaBattlePosition sbPos) {
        List<SeaBattleCell> cells = new LinkedList<>();
        cells.add(getCell(sbPos.getRelative(-1, 0)));
        cells.add(getCell(sbPos.getRelative(-1, 1)));
        cells.add(getCell(sbPos.getRelative(0, 1)));
        cells.add(getCell(sbPos.getRelative(1, 1)));
        cells.add(getCell(sbPos.getRelative(1, 0)));
        cells.add(getCell(sbPos.getRelative(1, -1)));
        cells.add(getCell(sbPos.getRelative(0, -1)));
        cells.add(getCell(sbPos.getRelative(-1, -1)));
        cells.stream().filter(Objects::nonNull).forEach(SeaBattleCell::strike);
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