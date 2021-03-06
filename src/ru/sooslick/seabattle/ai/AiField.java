package ru.sooslick.seabattle.ai;

import org.apache.commons.lang3.tuple.ImmutablePair;
import ru.sooslick.seabattle.entity.SeaBattleCell;
import ru.sooslick.seabattle.entity.SeaBattlePosition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AiField {
    private static final Random RANDOM = new Random();

    private final Supplier<PlacePosition> placeMethod;
    private final Supplier<String> shootMethod;

    private final SeaBattleCell[][] field;
    protected final List<Integer> ships;
    private final int shipsTotal;

    private int randomGuesses = 0;
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
        int jmod = 1 - imod;
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
                    int jmod = 1 - imod;
                    int z = 0;
                    while (true) {
                        z++;
                        SeaBattlePosition checkPos = detectedShip.getRelative(z * imod, z * jmod);
                        SeaBattleCell check = getCell(checkPos);
                        if (check == null || !check.hasShip())
                            break;
                        markKill(checkPos);
                        marked++;
                    }
                    z = 0;
                    while (true) {
                        z--;
                        SeaBattlePosition checkPos = detectedShip.getRelative(z * imod, z * jmod);
                        SeaBattleCell check = getCell(checkPos);
                        if (check == null || !check.hasShip())
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

    private List<PlacePosition> filterPlacePositions() {
        List<PlacePosition> positions = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (!field[i][j].hasShip())
                    positions.add(new PlacePosition(new SeaBattlePosition(i, j)));
        Collections.shuffle(positions);
        return positions;
    }

    private PlacePosition getRandomPlacePosition() {
        List<PlacePosition> positions = filterPlacePositions();
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
        List<PlacePosition> positions = filterPlacePositions();
        int maxShip = getMaxShip();
        if (maxShip <= 0)
            return null;
        for (PlacePosition pp : positions) {
            pp.size = maxShip;
            double hScore = calcPlaceScore(pp, false);
            double vScore = calcPlaceScore(pp, true);
            if (Math.abs(hScore - vScore) < 0.001) {
                pp.vert = RANDOM.nextBoolean();
                pp.score = Math.max(hScore, vScore);
            } else if (hScore > vScore) {
                pp.vert = false;
                pp.score = hScore;
            } else {
                pp.vert = true;
                pp.score = vScore;
            }
        }
        double maxScore = positions.stream().mapToDouble(pp -> pp.score).max().orElse(-1);
        if (maxScore < 0)
            return null;
        return positions.stream().filter(pp -> pp.score >= maxScore).findAny().orElse(null);
    }

    private String getBestShootPosition(boolean useHeatMult) {
        // guessing detected ship
        if (detectedShip != null) {
            List<SeaBattlePosition> guessNext = new LinkedList<>();
            if (detectedVert == null || detectedVert) {
                // check up / down
                for (int i = -1; i <= 1; i+= 2) {
                    int mod = 0;
                    while (true) {
                        SeaBattlePosition cPos = detectedShip.getRelative(i * ++mod, 0);
                        SeaBattleCell cCell = getCell(cPos);
                        // check OoB first,
                        if (cCell == null)
                            break;
                        // then if cell has ship, continue investigating,
                        if (cCell.hasShip())
                            continue;
                        // if next cell is striked, then break and try another direction,
                        if (cCell.isStriked())
                            break;
                        // finally, guess direction
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
                        SeaBattlePosition cPos = detectedShip.getRelative(0, i * ++mod);
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
            double maxScore = guessNext.stream().mapToDouble(pos -> calcShootScore(pos, useHeatMult)).max().orElse(1);
            Collections.shuffle(guessNext);
            return guessNext.stream().filter(pos -> calcShootScore(pos, useHeatMult) >= maxScore).findAny().orElse(guessNext.get(0)).toString();
        }
        // get available cells
        List<SeaBattlePosition> positions = new LinkedList<>();
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (!field[i][j].isStriked())
                    positions.add(new SeaBattlePosition(i, j));
        // guess cell by random
        int maxShip = getMaxShip();
        if ((ships.size() >= shipsTotal && randomGuesses++ < 10) || maxShip <= 1) {
            double maxScore = positions.stream().mapToDouble(pos -> calcShootScore(pos, useHeatMult)).max().orElse(1);
            Collections.shuffle(positions);
            return positions.stream().filter(pos -> calcShootScore(pos, useHeatMult) >= maxScore).findAny().orElse(positions.get(0)).toString();
        }
        // try to analyze board
        else {
            Map<SeaBattlePosition, Double> scoreMap = new HashMap<>();
            double maxScore = 0;
            for (SeaBattlePosition sbPos : positions) {
                double cScore = calcShootScore(sbPos, useHeatMult);
                List<ImmutablePair<Integer, Integer>> directions = Arrays.asList(
                        new ImmutablePair<>(-1, 0), new ImmutablePair<>(1, 0), new ImmutablePair<>(0, -1), new ImmutablePair<>(0, 1));
                for (ImmutablePair<Integer, Integer> direction : directions) {
                    for (int i = 1; i < maxShip; i++) {
                        SeaBattlePosition relPos = sbPos.getRelative(i * direction.getKey(), i * direction.getValue());
                        SeaBattleCell check = getCell(relPos);
                        if (check == null || check.isStriked())
                            break;
                        cScore += calcShootScore(relPos, useHeatMult);
                    }
                }
                scoreMap.put(sbPos, cScore);
                if (cScore > maxScore)
                    maxScore = cScore;
            }
            final double max = maxScore;
            List<SeaBattlePosition> selectedPos = scoreMap.entrySet().stream().filter(e -> e.getValue() >= max).map(Map.Entry::getKey).collect(Collectors.toList());
            Collections.shuffle(selectedPos);
            return selectedPos.get(0).toString();
        }
    }

    private String getRandomShootPosition() {
        return getBestShootPosition(false);
    }

    private String getHeatShootPosition() {
        return getBestShootPosition(true);
    }

    private double calcShootScore(SeaBattlePosition pos, boolean heat) {
        return heat ? AiHeatData.getMultiplier(pos) : 1;
    }

    private int getMaxShip() {
        return ships.stream().mapToInt(i -> i).max().orElse(0);
    }

    private boolean canPlace(PlacePosition pp) {
        int imod = pp.vert ? 1 : 0;
        int jmod = 1 - imod;
        for (int z = 1; z < pp.size; z++) {
            SeaBattlePosition sbPos = pp.positionRaw.getRelative(z * imod, z * jmod);
            if (sbPos.getRow() >= 10 || sbPos.getCol() >= 10)
                return false;
            if (field[sbPos.getRow()][sbPos.getCol()].hasShip())
                return false;
        }
        return true;
    }

    private double calcPlaceScore(PlacePosition pp, boolean vert) {
        pp.vert = vert;
        if (!canPlace(pp))
            return -1;
        int imod = vert ? 1 : 0;
        int jmod = 1 - imod;
        double score = 10;
        for (int z = 0; z < pp.size; z++) {
            SeaBattlePosition sbPos = pp.positionRaw.getRelative(z * imod, z * jmod);
            score-= AiHeatData.getMultiplier(sbPos);
        }
        return score;
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
        Stream.of(
                getCell(sbPos.getRelative(-1, 0)),
                getCell(sbPos.getRelative(-1, 1)),
                getCell(sbPos.getRelative(0, 1)),
                getCell(sbPos.getRelative(1, 1)),
                getCell(sbPos.getRelative(1, 0)),
                getCell(sbPos.getRelative(1, -1)),
                getCell(sbPos.getRelative(0, -1)),
                getCell(sbPos.getRelative(-1, -1))
        ).filter(Objects::nonNull).forEach(SeaBattleCell::strike);
    }

    public static class PlacePosition {
        private SeaBattlePosition positionRaw;
        private String position;
        private int size;
        private boolean vert;
        private double score = 0;

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