package ru.sooslick.seabattle.entity;

public class SeaBattleCell {
    private boolean ship = false;
    private boolean striked = false;

    public Integer getResult(boolean my) {
        int b1 = striked ? 1 : 0;           //first byte: is striked
        int b2 = my ?                       //second byte: is ship revealed
                (ship ? 2 : 0) :
                (striked && ship ? 2 : 0);
        return b1 + b2;
    }
}
