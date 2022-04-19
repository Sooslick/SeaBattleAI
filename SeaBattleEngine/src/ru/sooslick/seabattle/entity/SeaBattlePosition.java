package ru.sooslick.seabattle.entity;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeaBattlePosition {
    public static final Pattern POSITION_REGEX = Pattern.compile("^([a-j])([1-9]|10)$");

    private final int row;
    private final int col;

    public static boolean isValid(String position) {
        return position != null && POSITION_REGEX.matcher(position).matches();
    }

    public static @Nullable SeaBattlePosition convertPosition(String position) {
        Matcher m = POSITION_REGEX.matcher(position);
        return m.matches() ? new SeaBattlePosition(getPositionRow(m.group(2)), getPositionCol(m.group(1))) : null;
    }

    private static int getPositionRow(String s) {
        return Integer.parseInt(s) - 1;
    }

    private static int getPositionCol(String s) {
        return (int) s.charAt(0) - 97;
    }

    public SeaBattlePosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public SeaBattlePosition getRelative(int i, int j) {
        return new SeaBattlePosition(row + i, col + j);
    }

    @Override
    public String toString() {
        return (char) (col + 97) + Integer.toString(row + 1);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (!(other instanceof SeaBattlePosition))
            return false;
        SeaBattlePosition pos = (SeaBattlePosition) other;
        if (row != pos.getRow())
            return false;
        return col == pos.getCol();
    }

    @Override
    public int hashCode() {
        return row * 13 + col * 139;
    }
}
