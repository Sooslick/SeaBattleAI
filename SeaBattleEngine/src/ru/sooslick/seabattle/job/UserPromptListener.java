package ru.sooslick.seabattle.job;

import java.util.Scanner;

public class UserPromptListener extends Thread {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.hasNext())
                if ('q' == scanner.next().charAt(0))
                    break;
        }
    }
}
