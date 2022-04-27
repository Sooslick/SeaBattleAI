package ru.sooslick.seabattle.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.test.scenarios.CellTest;
import ru.sooslick.seabattle.test.scenarios.EventsTest;
import ru.sooslick.seabattle.test.scenarios.FieldTest;
import ru.sooslick.seabattle.test.scenarios.HandlerTest;
import ru.sooslick.seabattle.test.scenarios.MainTest;
import ru.sooslick.seabattle.test.scenarios.PlayerTest;
import ru.sooslick.seabattle.test.scenarios.PositionTest;
import ru.sooslick.seabattle.test.scenarios.PromptTest;
import ru.sooslick.seabattle.test.scenarios.SessionTest;

public class TestRunner {
    public static void main(String[] args) {
        JUnitCore core = new JUnitCore();
        Result result = core.run(
                CellTest.class,
                PositionTest.class,
                FieldTest.class,
                PlayerTest.class,
                SessionTest.class,
                HandlerTest.class,
                MainTest.class,
                PromptTest.class,
                EventsTest.class
        );
        result.getFailures().forEach(f -> {
            Log.warn("\nFailed test " + f.getTestHeader());
            f.getException().printStackTrace();
        });
        Log.info("Tests finished in " + result.getRunTime() + "ms");
    }
}
