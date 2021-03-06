package ru.sooslick.seabattle.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.test.scenarios.*;

public class TestRunner {
    public static void main(String[] args) {
        System.setProperty("use.defaults", "true");
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
        if (result.getFailureCount() == 0)
            Log.info("* S U C C E S S *");
        else
            result.getFailures().forEach(f -> {
                Log.warn("\nFailed test " + f.getTestHeader());
                f.getException().printStackTrace();
            });
        Log.info("Tests finished in " + result.getRunTime() + "ms");
        Log.info(result.getRunCount() - result.getFailureCount() + "/" + result.getRunCount() + " tests passed successfully");
    }
}
