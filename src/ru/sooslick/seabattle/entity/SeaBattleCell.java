package ru.sooslick.seabattle.entity;

/**
 * Field cell entity
 */
public class SeaBattleCell {
    private boolean ship = false;
    private boolean striked = false;

    public boolean hasShip() {
        return ship;
    }

    public void placeShip() {
        ship = true;
    }

    public boolean isStriked() {
        return striked;
    }

    public void strike() {
        striked = true;
    }

    /**
     * Get number value for cell state
     * @param my true for own field. If false, ships aren't revealed before they are striked
     * @return number value for cell state
     */
    public Integer getResult(boolean my) {
        int b1 = striked ? 1 : 0;           //first byte: is striked
        int b2 = my ?                       //second byte: is ship revealed
                (ship ? 2 : 0) :
                (striked && ship ? 2 : 0);
        return b1 + b2;
    }
}
