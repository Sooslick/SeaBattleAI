package ru.sooslick.seabattle.test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import ru.sooslick.seabattle.Log;
import ru.sooslick.seabattle.test.scenarios.FieldTest;

public class TestRunner {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(FieldTest.class);
        result.getFailures().forEach(f -> {
            Log.warn("\nFailed test " + f.getTestHeader());
            f.getException().printStackTrace();
        });
        Log.info("Tests finished");
    }
}
