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
        placeAndCheckResult("a1", 0, false, false, "Failed placeShip: no such ship that size");
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        placeAndCheckResult("a1", -1, false, false, "Failed placeShip: no such ship that size");
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        placeAndCheckResult("a1", 5, false, false, "Failed placeShip: no such ship that size");
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        // out of bounds
        placeAndCheckResult("j10", 2, false, false, "Failed placeShip: out of bounds");
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        placeAndCheckResult("j10", 2, true, false, "Failed placeShip: out of bounds");
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(startShips, field.getShips()));
        checkFieldFree(field.getResult(true));
        checkFieldFree(field.getResult(false));

        // success single ship
        placeAndCheckResult("a1", 1, true, true, null);
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships1, field.getShips()));
        checkFieldFree(field.getResult(false));
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 0, getStrikedCellsCount(field.getResult(true)));

        // all ships size of "1"
        placeAndCheckResult("a10", 1, false, true, null);
        placeAndCheckResult("j1", 1, true, true, null);
        placeAndCheckResult("j10", 1, false, true, null);
        placeAndCheckResult("e5", 1, false, false, "Failed placeShip: no such ship that size");
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships2, field.getShips()));
        Assert.assertEquals("Ship cells count not equals", 4, getShipCellsCount(field.getResult(true)));

        // one of 2, 3, 4 ships
        placeAndCheckResult("c1", 2, false, true, null);
        placeAndCheckResult("a3", 3, true, true, null);
        placeAndCheckResult("j5", 4, true, true, null);
        Assert.assertTrue("Ships not equals", CollectionUtils.isEqualCollection(ships3, field.getShips()));

        // remaining ships
        placeAndCheckResult("c3", 2, false, true, null);
        placeAndCheckResult("c5", 3, false, true, null);
        placeAndCheckResult("g3", 2, true, true, null);
        Assert.assertTrue("Ships remains", field.getShips().isEmpty());
        Assert.assertEquals("Ship cells count not equals", 20, getShipCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 0, getStrikedCellsCount(field.getResult(true)));
        checkFieldFree(field.getResult(false));

        // try place after 0 ships remains
        placeAndCheckResult("g7", 1, false, false, "Failed placeShip: no such ship that size");

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
        SeaBattleProperties.GAME_STRIKED_CHECK_ENABLE = true;

        placeAndCheckResult("c3", 1, false, true, null);
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place on exact pos
        placeAndCheckResult("c3", 1, false, false, "Failed placeShip: collision");
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place around
        placeAndCheckResult("b2", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("b3", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("b4", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("c2", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("c4", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("d2", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("d3", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("d4", 1, false, false, "Failed placeShip: collision");
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // successful ships around
        placeAndCheckResult("c1", 2, false, true, null);
        placeAndCheckResult("c5", 2, false, true, null);
        placeAndCheckResult("a2", 3, true, true, null);
        placeAndCheckResult("e3", 1, true, true, null);
        Assert.assertEquals("Ship cells count not equals", 9, getShipCellsCount(field.getResult(true)));

        // kill and check striked cells
        shootAndCheckResult("c3", true, "kill");
        Assert.assertEquals("Striked cells count not equals", 9, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 9, getStrikedCellsCount(field.getResult(false)));
        shootAndCheckResult("b2", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("b3", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("b4", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("c2", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("c3", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("c4", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("d2", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("d3", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("d4", false, "Failed shoot: this cell is striked");
    }

    @Test
    public void testPlaceCornerEnabled() {
        SeaBattleProperties.GAME_CORNER_COLLISION_ENABLE = true;
        SeaBattleProperties.GAME_STRIKED_CHECK_ENABLE = true;

        placeAndCheckResult("c3", 1, false, true, null);
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place on exact pos
        placeAndCheckResult("c3", 1, false, false, "Failed placeShip: collision");
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // cannot place around
        placeAndCheckResult("b3", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("c2", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("c4", 1, false, false, "Failed placeShip: collision");
        placeAndCheckResult("d3", 1, false, false, "Failed placeShip: collision");
        Assert.assertEquals("Ship cells count not equals", 1, getShipCellsCount(field.getResult(true)));

        // successful ships around
        placeAndCheckResult("b2", 1, false, true, null);
        placeAndCheckResult("b4", 1, false, true, null);
        placeAndCheckResult("d2", 1, true, true, null);
        placeAndCheckResult("d4", 2, true, true, null);
        Assert.assertEquals("Ship cells count not equals", 6, getShipCellsCount(field.getResult(true)));

        // kill and check striked cells
        shootAndCheckResult("c3", true, "kill");
        Assert.assertEquals("Striked cells count not equals", 5, getStrikedCellsCount(field.getResult(true)));
        Assert.assertEquals("Striked cells count not equals", 5, getStrikedCellsCount(field.getResult(false)));
        shootAndCheckResult("b3", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("c2", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("c3", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("c4", false, "Failed shoot: this cell is striked");
        shootAndCheckResult("d3", false, "Failed shoot: this cell is striked");
    }

    @Test
    public void testWin() {
        field.placeShip("b2", 1, false);
        shootAndCheckResult("b2", true, "win");
    }

    @Test
    public void testCheckStriked() {
        SeaBattleProperties.GAME_STRIKED_CHECK_ENABLE = false;

        placeAndCheckResult("a1", 1, false, true, null);
        placeAndCheckResult("a3", 2, false, true, null);

        // miss and repeat
        shootAndCheckResult("j10", true, "miss");
        shootAndCheckResult("j10", true, "miss");

        // kill, repeat and try hit around
        shootAndCheckResult("a1", true, "kill");
        shootAndCheckResult("a1", true, "kill");
        shootAndCheckResult("a2", true, "miss");

        // hit, repeat and kill
        shootAndCheckResult("a3", true, "hit");
        shootAndCheckResult("a3", true, "hit");
        shootAndCheckResult("b3", true, "win");
    }

    private void checkFieldFree(FieldResult fr) {
        fr.getRows().forEach(row -> row.getCols().forEach(col -> Assert.assertEquals("Cell is marked", 0, (int) col)));
    }

    private int getShipCellsCount(FieldResult fr) {
        return fr.getRows().stream().mapToInt(row -> row.getCols().stream().mapToInt(i -> i / 2).sum()).sum();
    }

    private int getStrikedCellsCount(FieldResult fr) {
        return fr.getRows().stream().mapToInt(row -> row.getCols().stream().mapToInt(i -> i % 2).sum()).sum();
    }

    private void placeAndCheckResult(String pos, int size, boolean vert, boolean expectedSuccess, String expectedResolution) {
        EventResult sr = field.placeShip(pos, size, vert);
        Assert.assertEquals("Unexpected event result", expectedSuccess, sr.getSuccess());
        Assert.assertEquals("Unexpected place result", expectedResolution, sr.getInfo());
    }

    private void shootAndCheckResult(String pos, boolean expectedSuccess, String expectedResolution) {
        EventResult sr = field.shoot(pos);
        Assert.assertEquals("Unexpected event result", expectedSuccess, sr.getSuccess());
        Assert.assertEquals("Unexpected shot result", expectedResolution, sr.getInfo());
    }
}
