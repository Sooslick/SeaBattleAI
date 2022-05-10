package ru.sooslick.seabattle.test.scenarios;

import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.entity.SeaBattlePosition;

import java.util.Random;

public class PositionTest {
    @Test
    public void test() {
        Random random = new Random();
        int r = random.nextInt(10);
        int c = random.nextInt(10);
        SeaBattlePosition startPos = new SeaBattlePosition(r, c);
        Assert.assertEquals("Row not equals", r, startPos.getRow());
        Assert.assertEquals("Col not equals", c, startPos.getCol());

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                SeaBattlePosition relPos = startPos.getRelative(i, j);
                Assert.assertEquals("Row not equals", r + i, relPos.getRow());
                Assert.assertEquals("Col not equals", c + j, relPos.getCol());
            }
        }
    }

    @Test
    public void testEquals() {
        for (int i = 'a'; i <= 'j'; i++) {
            for (int j = 1; j <= 10; j++) {
                String rawPos = (char) i + Integer.toString(j);
                SeaBattlePosition strPos = SeaBattlePosition.convertPosition(rawPos);
                SeaBattlePosition numPos = new SeaBattlePosition(j - 1, i - 'a');
                Assert.assertNotNull("Position is null", strPos);
                Assert.assertEquals("Positions not equals", numPos, strPos);
                Assert.assertEquals("Positions not equals", rawPos, strPos.toString());
                Assert.assertEquals("Positions not equals", rawPos, numPos.toString());
            }
        }
    }

    @Test
    public void validationTest() {
        // positive tests
        for (int i = 'a'; i <= 'j'; i++) {
            for (int j = 1; j <= 10; j++) {
                Assert.assertTrue("non valid position", SeaBattlePosition.isValid((char) i + Integer.toString(j)));
            }
        }
        // negative tests
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("A1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("1a"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a0"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a11"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a21"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a100"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("aa1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("k1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("aA"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("?a1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a1a"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid(""));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid(null));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a 1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a-1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a\n1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid(" a1"));
        Assert.assertFalse("valid position", SeaBattlePosition.isValid("a1 "));
    }
}
