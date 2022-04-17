package ru.sooslick.seabattle.test.scenarios;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.SeaBattleProperties;
import ru.sooslick.seabattle.entity.SeaBattleField;
import ru.sooslick.seabattle.result.EventResult;
import ru.sooslick.seabattle.result.FieldResult;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FieldTest {
    SeaBattleField field = new SeaBattleField();

    @Test
    public void testBasic() {
        SeaBattleProperties.GAME_CORNER_COLLISION_ENABLE = false;
        SeaBattleProperties.GAME_STRIKED_CHECK_ENABLE = true;

        List<Integer> startShips = new LinkedList<>(Arrays.asList(4, 3, 3, 2, 2, 2, 1, 1, 1, 1));
        List<Integer> ships1 = new LinkedList<>(Arrays.asList(4, 3, 3, 2, 2, 2, 1, 1, 1));
        List<Integer> ships2 = new LinkedList<>(Arrays.asList(4, 3, 3, 2, 2, 2));
        List<Integer> ships3 = new LinkedList<>(Arrays.asList(3, 2, 2));

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
        // todo validate fail messages
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
        Assert.assertTrue("Unsuccessful event result", field.placeShip("a1", 1, true).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships1, field.getShips()));
        checkFieldFree(field.getResult(false));
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 0, getStrikedCellsCount(field.getResult(true)));

        // all ships size of "1"
        Assert.assertTrue("Unsuccessful event result", field.placeShip("a10", 1, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("j1", 1, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("j10", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("e5", 1, false).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships2, field.getShips()));
        Assert.assertEquals("Ship cells count not equals", 4, getShipCellsCount(field.getResult(true)));

        // one of 2, 3, 4 ships
        Assert.assertTrue("Unsuccessful event result", field.placeShip("c1", 2, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("a3", 3, true).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("j5", 4, true).getSuccess());
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships3, field.getShips()));

        // remaining ships
        Assert.assertTrue("Unsuccessful event result", field.placeShip("c3", 2, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("c5", 3, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("g3", 2, true).getSuccess());
        Assert.assertTrue("Ships remains", field.getShips().isEmpty());
        Assert.assertEquals("Ship cells count not equals", 20, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 0, getStrikedCellsCount(field.getResult(true)));
        checkFieldFree(field.getResult(false));

        // try place after 0 ships remains
        Assert.assertFalse("Successful event result", field.placeShip("g7", 1, false).getSuccess());

        // shoot tests
        // miss + check fields
        shootAndCheckResult("a2", true, "miss");
        Assert.assertEquals("Ship cells count not equals", 20, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 1, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Ship cells count not equals", 0, getShipCellsCount(field.getResult(false)));
        Assert.assertEquals("Striked cells count not equals", 1, getStrikedCellsCount(field.getResult(false)));

        shootAndCheckResult("a2", false, "Failed shoot: this cell is striked");
        Assert.assertEquals("Ship cells count not equals", 20, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 1, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Ship cells count not equals", 0, getShipCellsCount(field.getResult(false)));
        Assert.assertEquals("Striked cells count not equals", 1, getStrikedCellsCount(field.getResult(false)));

        // single kill
        shootAndCheckResult("a1", true, "kill");
        Assert.assertEquals("Ship cells count not equals", 20, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 4, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(false)));
        Assert.assertEquals("Striked cells count not equals", 4, getStrikedCellsCount(field.getResult(false)));

        // consistent hits
        shootAndCheckResult("c1", true, "hit");
        Assert.assertEquals("Ship cells count not equals", 20, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 5, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Ship cells count not equals", 2, getShipCellsCount(field.getResult(false)));
        Assert.assertEquals("Striked cells count not equals", 5, getStrikedCellsCount(field.getResult(false)));
        shootAndCheckResult("d1", true, "kill");
        Assert.assertEquals("Striked cells count not equals", 10, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 10, getStrikedCellsCount(field.getResult(false)));

        // unconsistent hits
        shootAndCheckResult("a3", true, "hit");
        shootAndCheckResult("b3", true, "miss");
        shootAndCheckResult("a5", true, "hit");
        shootAndCheckResult("a6", true, "miss");
        shootAndCheckResult("j5", true, "hit");
        shootAndCheckResult("a4", true, "kill");
    }

    @Test
    public void testPlaceCornerDisabled() {
        SeaBattleProperties.GAME_CORNER_COLLISION_ENABLE = false;
        SeaBattleField field = new SeaBattleField();

        Assert.assertTrue("Unsuccessful event result", field.placeShip("c3", 1, false).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place on exact pos
        Assert.assertFalse("Successful event result", field.placeShip("c3", 1, false).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place around
        Assert.assertFalse("Successful event result", field.placeShip("b2", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("b3", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("b4", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("c2", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("c4", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("d2", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("d3", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("d4", 1, false).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // successful ships around
        Assert.assertTrue("Unsuccessful event result", field.placeShip("c1", 2, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("c5", 2, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("a2", 3, true).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("e3", 1, true).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 9, getShipCellsCount(field.getResult(true)));

        // todo kill and check striked cells
    }

    @Test
    public void testPlaceCornerEnabled() {
        SeaBattleProperties.GAME_CORNER_COLLISION_ENABLE = true;
        SeaBattleField field = new SeaBattleField();

        Assert.assertTrue("Unsuccessful event result", field.placeShip("c3", 1, false).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place on exact pos
        Assert.assertFalse("Successful event result", field.placeShip("c3", 1, false).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place around
        Assert.assertFalse("Successful event result", field.placeShip("b3", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("c2", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("c4", 1, false).getSuccess());
        Assert.assertFalse("Successful event result", field.placeShip("d3", 1, false).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // successful ships around
        Assert.assertTrue("Unsuccessful event result", field.placeShip("b2", 1, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("b4", 1, false).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("d2", 1, true).getSuccess());
        Assert.assertTrue("Unsuccessful event result", field.placeShip("d4", 2, true).getSuccess());
        Assert.assertEquals("Ship cells count not equals", 6, getShipCellsCount(field.getResult(true)));

        // todo kill and check striked cells
    }

    @Test
    public void testWin() {
        field.placeShip("b2", 1, false);
        shootAndCheckResult("b2", true, "win");
    }

    // todo striked check enabled test

    private void checkFieldFree(FieldResult fr) {
        fr.getRows().forEach(row -> row.getCols().forEach(col -> Assert.assertEquals("Cell is marked", 0, (int) col)));
    }

    private int getShipCellsCount(FieldResult fr) {
        return fr.getRows().stream().mapToInt(row -> row.getCols().stream().mapToInt(i -> i / 2).sum()).sum();
    }

    private int getStrikedCellsCount(FieldResult fr) {
        return fr.getRows().stream().mapToInt(row -> row.getCols().stream().mapToInt(i -> i % 2).sum()).sum();
    }

    private void shootAndCheckResult(String pos, boolean expectedSuccess, String expectedResolution) {
        EventResult sr = field.shoot(pos);
        Assert.assertEquals("Unexpected event result", expectedSuccess, sr.getSuccess());
        Assert.assertEquals("Unexpected shot result", expectedResolution, sr.getInfo());
    }
}
