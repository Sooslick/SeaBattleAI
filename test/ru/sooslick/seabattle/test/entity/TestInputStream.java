package ru.sooslick.seabattle.test.entity;

import org.apache.commons.lang3.ArrayUtils;
import ru.sooslick.seabattle.Log;

import java.io.InputStream;

public class TestInputStream extends InputStream {
    private byte[] nextBytes;
    private int cpos = 0;
    private int previousResponse = 0;

    boolean waiting = true;
    boolean closed = false;

    public TestInputStream(String source) {
        nextBytes = source.getBytes();
    }

    @Override
    public synchronized int read() {
        if (cpos >= nextBytes.length && previousResponse == -1)
            waitForPrompt();
        previousResponse = cpos < nextBytes.length ? nextBytes[cpos++] : -1;
        return previousResponse;
    }

    public void prompt(String prompt) {
        if (closed)
            throw new IllegalStateException("TestInputStream is closed");

        nextBytes = ArrayUtils.addAll(nextBytes, prompt.getBytes());
        Log.info("TestInputStream set prompt " + prompt);

        wakeup();
    }

    public boolean isWaiting() {
        return waiting;
    }

    @Override
    public void close() {
        closed = true;
        wakeup();
    }

    private synchronized void wakeup() {
        waiting = false;
        this.notify();
    }

    private synchronized void waitForPrompt() {
        try {
            Log.info("TestInputStream wait start");
            waiting = true;
            this.wait();
            waiting = false;
            Log.info("TestInputStream wait end");
        } catch (InterruptedException e) {
            waiting = false;
        }
    }
}
