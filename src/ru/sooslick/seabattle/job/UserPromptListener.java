package ru.sooslick.seabattle.job;

import ru.sooslick.seabattle.Log;

import java.io.IOException;
import java.util.Scanner;

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
        Scanner scanner = new Scanner(System.in);
        do {
            if (scanner.hasNextLine()) {
                String prompt = scanner.nextLine();
                if (prompt.startsWith("q") || prompt.startsWith("Q")) {
                    scanner.close();
                    break;
                }
            }
        } while (!isInterrupted());
        Log.info("Quitting...");
    }
}
