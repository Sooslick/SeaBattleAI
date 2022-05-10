package ru.sooslick.seabattle.test.entity;

import java.io.InputStream;

public class TestInputStream extends InputStream {
    private byte[] nextBytes;
    private int cpos = 0;

    public TestInputStream(String source) {
        nextBytes = source.getBytes();
    }

    @Override
    public int read() {
        return cpos < nextBytes.length ? nextBytes[cpos++] : '\n';
    }

    public void prompt(String prompt) {
        cpos = 0;
        nextBytes = prompt.getBytes();
    }
}
