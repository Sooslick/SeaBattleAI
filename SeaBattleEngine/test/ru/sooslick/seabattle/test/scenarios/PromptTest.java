package ru.sooslick.seabattle.test.scenarios;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import ru.sooslick.seabattle.job.UserPromptListener;
import ru.sooslick.seabattle.test.entity.TestInputStream;

import java.util.concurrent.TimeUnit;

public class PromptTest {
    private final UserPromptListener upl = new UserPromptListener();

    @Test
    public void test() throws InterruptedException {
        TestInputStream is = new TestInputStream("test\n");
        System.setIn(is);

        upl.start();
        TimeUnit.MILLISECONDS.sleep(50);
        Assert.assertTrue("RIP prompt listener", upl.isAlive());

        is.prompt("aqua\n");
        TimeUnit.MILLISECONDS.sleep(50);
        Assert.assertTrue("RIP prompt listener", upl.isAlive());

        is.prompt("quit\n");
        TimeUnit.MILLISECONDS.sleep(50);
        Assert.assertFalse("Prompt listener is alive", upl.isAlive());
    }

    @After
    public void stopListener() {
        if (upl.isAlive())
            upl.interrupt();
    }
}
