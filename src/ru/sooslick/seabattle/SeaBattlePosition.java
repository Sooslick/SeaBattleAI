package ru.sooslick.seabattle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeaBattlePosition {
    public static final Pattern POSITION_REGEX = Pattern.compile("^([a-j])([1-9]|10)$");

    private int row;
    private int col;

    public static boolean isValid(String position) {
        return POSITION_REGEX.matcher(position).matches();
    }

    public static SeaBattlePosition convertPosition(String position) {
        Matcher m = POSITION_REGEX.matcher(position);
        return new SeaBattlePosition(getPositionRow(m.group(2)), getPositionCol(m.group(1)));
    }

    private static int getPositionRow(String s) {
        return Integer.parseInt(s) - 1;
    }

    private static int getPositionCol(String s) {
        return (int) s.charAt(0) - 97;
    }

    private SeaBattlePosition(int row, int col) {
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
}
