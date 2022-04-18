package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.entity.SeaBattleCell;

public class CellTest {
    @Test
    public void test00() {
        SeaBattleCell cell = new SeaBattleCell();
        Assert.assertFalse("Cell is marked", cell.hasShip());
        Assert.assertFalse("Cell is marked", cell.isStriked());
        Assert.assertEquals("Result is not equal", 0, (int) cell.getResult(true));
        Assert.assertEquals("Result is not equal", 0, (int) cell.getResult(false));
    }

    @Test
    public void test01() {
        SeaBattleCell cell = new SeaBattleCell();
        cell.strike();
        Assert.assertFalse("Cell is marked", cell.hasShip());
        Assert.assertTrue("Cell is not marked", cell.isStriked());
        Assert.assertEquals("Result is not equal", 1, (int) cell.getResult(true));
        Assert.assertEquals("Result is not equal", 1, (int) cell.getResult(false));
    }

    @Test
    public void test10() {
        SeaBattleCell cell = new SeaBattleCell();
        cell.placeShip();
        Assert.assertTrue("Cell is not marked", cell.hasShip());
        Assert.assertFalse("Cell is marked", cell.isStriked());
        Assert.assertEquals("Result is not equal", 2, (int) cell.getResult(true));
        Assert.assertEquals("Result is not equal", 0, (int) cell.getResult(false));
    }

    @Test
    public void test11() {
        SeaBattleCell cell = new SeaBattleCell();
        cell.placeShip();
        cell.strike();
        Assert.assertTrue("Cell is not marked", cell.hasShip());
        Assert.assertTrue("Cell is not marked", cell.isStriked());
        Assert.assertEquals("Result is not equal", 3, (int) cell.getResult(true));
        Assert.assertEquals("Result is not equal", 3, (int) cell.getResult(false));
    }
}
