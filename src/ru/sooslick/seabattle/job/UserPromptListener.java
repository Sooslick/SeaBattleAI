package ru.sooslick.seabattle.job;

import ru.sooslick.seabattle.Log;

import java.io.IOException;

/**
 * Listen console commands. Only quit command available :)
 */
public class UserPromptListener extends Thread {
    private static UserPromptListener instance;

    public static void forceQuit() {
        Log.info("Quit command received");
        instance.interrupt();
    }

    public UserPromptListener() {
        instance = this;
    }

    @Override
    public void run() {
        int bytesAvailable;
        byte[] buffer = new byte[256];
        do {
            try {
                bytesAvailable = System.in.available();
                if (bytesAvailable > 0) {
                    System.in.read(buffer);
                    if (hasQuitCommand(buffer))
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!isInterrupted());
        Log.info("Quitting...");
    }

    private boolean hasQuitCommand(byte[] test) {
        for (int i = 0; i < test.length; i++) {
            if (test[i] == (byte) 'q' || test[i] == (byte) 'Q')
                return true;
        }
        return false;
    }
}
