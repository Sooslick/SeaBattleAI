package ru.sooslick.seabattle.test.scenarios;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.entity.SeaBattleField;
import ru.sooslick.seabattle.result.FieldResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FieldTest {
    @Test
    public void test() {
        SeaBattleField field = new SeaBattleField();
        List<Integer> startShips = new LinkedList<>(Arrays.asList(4, 3, 3, 2, 2, 2, 1, 1, 1, 1));
        List<Integer> ships1 = new LinkedList<>(Arrays.asList(4, 3, 3, 2, 2, 2, 1, 1, 1));

        FieldResult fr = field.getResult(true);
        Assert.assertEquals("Rows number not equals", 10, fr.getRows().size());
        fr.getRows().forEach(row ->
                Assert.assertEquals("Cols number not equals", 10, row.getCols().size())
        );
        checkFieldFree(fr);

        Assert.assertEquals("Ships count not equals", 10, field.getShips().size());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));

        // place tests
        // invalid size
        Assert.assertFalse("Successful event result", field.placeShip("a1", 0, false).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        Assert.assertFalse("Successful event result", field.placeShip("a1", -1, false).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        Assert.assertFalse("Successful event result", field.placeShip("a1", 5, false).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        // out of bounds
        Assert.assertFalse("Successful event result", field.placeShip("j10", 2, false).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        Assert.assertFalse("Successful event result", field.placeShip("j10", 2, true).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        // success single ship
        Assert.assertTrue("Unsuccessful event result", field.placeShip("c3", 1, true).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships1, field.getShips()));
        checkFieldFree(field.getResult(false));
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // todo collision validation

        // todo place vert check
        // todo place hor check
        // todo empty ships validation

        // todo shoot tests

        // todo tests for various properties
    }

    public void checkFieldFree(FieldResult fr) {
        fr.getRows().forEach(row -> row.getCols().forEach(col -> Assert.assertEquals("Cell is marked", 0, (int) col)));
    }

    public int getShipCellsCount(FieldResult fr) {
        return fr.getRows().stream().mapToInt(row -> row.getCols().stream().mapToInt(i -> i / 2).sum()).sum();
    }
}
