package ru.sooslick.seabattle.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.test.scenarios.*;

public class TestRunner {
    public static void main(String[] args) {
        JUnitCore core = new JUnitCore();
        //core.addListener(new TestListener());
        Result result = core.run(CellTest.class, PositionTest.class, FieldTest.class, PlayerTest.class, SessionTest.class);
        result.getFailures().forEach(f -> {
            Log.warn("\nFailed test " + f.getTestHeader());
            f.getException().printStackTrace();
        });
        Log.info("Tests finished");
    }
}
